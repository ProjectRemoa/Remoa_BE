package Remoa.BE.Web.Post.Service;

import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Domain.UploadFile;
import Remoa.BE.Web.Post.Repository.PostRepository;
import Remoa.BE.Web.Post.Repository.UploadFileRepository;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import Remoa.BE.exception.response.BaseResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final AmazonS3 amazonS3;
    private final UploadFileRepository uploadFileRepository;
    private final PostRepository postRepository;
    private final List<UploadFile> uploadFileList;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public void deleteByPost(Post post){
        uploadFileRepository.deleteByPost(post);
    }

    /**
     * @param post          게시글
     * @param multipartFile 해당 게시글의 파일 리스트
     *                      파일들을 저장해준다
     */
    @Transactional
    public void saveUploadFiles(Post post, MultipartFile thumbnail, List<MultipartFile> multipartFile) {

        //썸네일 파일 저장 추가
        saveUploadFile(thumbnail, post, "thumbnail");
        if (multipartFile != null) {
            multipartFile.forEach(file -> saveUploadFile(file, post, "post"));
        }

        //새로운 인스턴스 만들어서 set하지 않으면 clear 되면서 null이 계속 저장됨.
        UploadFile uploadFile = uploadFileList.get(0);
        post.setThumbnailUrl(uploadFile.getStoreFileUrl());

        post.setUploadFiles(new ArrayList<>(uploadFileList.subList(1, uploadFileList.size())));
        postRepository.savePost(post);

        uploadFileList.clear();
    }

    /**
     * @param post          수정할 게시글
     * @param multipartFile 해당 게시글의 수정할 파일 리스트
     *                      Post 엔티티의 file들을 수정해준다
     */
    @Transactional
    public void modifyUploadFiles(Post post, MultipartFile thumbnail, List<MultipartFile> multipartFile) {

        List<UploadFile> recentFiles = uploadFileRepository.findFilesByPost(post);
        // 이미 해당하는 post에 파일 정보를 삭제처리
        if (recentFiles.size() > 0) {
            recentFiles.forEach(file -> {
                file.setDeleted(true); // DB 삭제 처리(delete 컬럼 update 1)
                uploadFileRepository.modifyFile(file);
                //      amazonS3.deleteObject(new DeleteObjectRequest(bucket, file.getSaveFileName())); // S3에서 삭제처리
            });
        }

        //썸네일 파일 저장 추가
        saveUploadFile(thumbnail, post, "thumbnail");
        if (multipartFile != null) {
            multipartFile.forEach(file -> saveUploadFile(file, post, "post"));
        }

        //새로운 인스턴스 만들어서 set하지 않으면 clear 되면서 null이 계속 저장됨.
        UploadFile uploadFile = uploadFileList.get(0);
        post.setThumbnailUrl(uploadFile.getStoreFileUrl());

        post.setUploadFiles(new ArrayList<>(uploadFileList.subList(1, uploadFileList.size())));
        postRepository.modifyPost(post);

        uploadFileList.clear();
    }

    /**
     * @param multipartFile 파일
     */
    @Transactional
    public void saveUploadFile(MultipartFile multipartFile, Post post, String folderName) {
        String originalFilename = validateAndAdjustFileName(multipartFile);

        String s3name = generateUniqueFileName(folderName, originalFilename);

        uploadToS3(multipartFile, s3name);

        String storeFileUrl = getAmazonS3Url(bucket, s3name);

        validateAndSaveUploadFile(originalFilename, s3name, storeFileUrl, post);
    }

    private String validateAndAdjustFileName(MultipartFile multipartFile) {
        // 파일 이름
        String originalFilename = multipartFile.getOriginalFilename();
        assert originalFilename != null;

        // 파일 이름 길이 검사 및 조정
        if (originalFilename.length() > 255) {
            log.info("originalFilename too long: {}", originalFilename);
            originalFilename = originalFilename.substring(0, 255);
        }
        return originalFilename;
    }

    private String generateUniqueFileName(String folderName, String originalFilename) {
        // 파일 이름이 겹치지 않게
        String uuid = UUID.randomUUID().toString();
        String s3name = folderName + "/" + uuid + "_" + originalFilename;

        // 파일 이름 길이 검사
        if (s3name.length() > 500) {
            log.info("s3name too long: {}", s3name);
            throw new BaseException(CustomMessage.INVALID_FILE_LENGTH);
        }
        return s3name;
    }

    private void uploadToS3(MultipartFile multipartFile, String s3name) {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            // 파일 업로드
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());

            amazonS3.putObject(new PutObjectRequest(bucket, s3name, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            // 파일을 제대로 받아오지 못했을 때 예외 처리
            throw new RuntimeException(e);
        }
    }

    private String getAmazonS3Url(String bucket, String s3name) {
        // 파일 보관 URL
        String storeFileUrl = amazonS3.getUrl(bucket, s3name).toString().replaceAll("\\+", "+");

        // URL 길이 검사
        if (storeFileUrl.length() > 500) {
            log.info("storeFileUrl too long: {}", storeFileUrl);
            throw new BaseException(CustomMessage.INVALID_FILE_LENGTH);
        }
        return storeFileUrl;
    }

    private void validateAndSaveUploadFile(String originalFilename, String s3name, String storeFileUrl, Post post) {
        UploadFile uploadFile = new UploadFile();
        uploadFile.setOriginalFileName(originalFilename);
        uploadFile.setSaveFileName(s3name);
        uploadFile.setStoreFileUrl(storeFileUrl);
        uploadFile.setExtension(originalFilename.substring(originalFilename.lastIndexOf(".") + 1));
        uploadFile.setPost(post);

        // 데이터베이스에 저장
        uploadFileRepository.saveFile(uploadFile);
        uploadFileList.add(uploadFile);
        // 로깅
        log.info("Stored file URL: {}", storeFileUrl);
    }


    public String getUrl(Long fileId) {
        Optional<UploadFile> file = uploadFileRepository.findById(fileId);
        if (file.isPresent()) {
            return file.get().getStoreFileUrl();
        } else {
            //해당 파일이 없을떄 예외처리
            //Todo 예외처리 custom 따로 만들기
            throw new RuntimeException();
        }
    }


    public ResponseEntity<byte[]> getObject(Long fileId) throws IOException {

        Optional<UploadFile> file = uploadFileRepository.findById(fileId);
        if (file.isPresent()) {
            String s3name = file.get().getSaveFileName();

            S3Object o = amazonS3.getObject(new GetObjectRequest(bucket, s3name));
            S3ObjectInputStream objectInputStream = o.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(objectInputStream);

            String fileOriginalName = file.get().getOriginalFileName();
            //encode 메서드에 두 번째 파라메터에 StandardCharsets.UTF_8만 쓰면 오류가 나서 뒤에 name을 임사방편으로 붙임. 기능상 문제는 없을듯 함
            String fileNameFix = URLEncoder.encode(fileOriginalName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentLength(bytes.length);
            httpHeaders.setContentDispositionFormData("attachment", fileNameFix);

            return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
        } else {
            //해당 파일이 없을떄 예외처리
            //Todo 예외처리 custom 따로 만들기
            throw new RuntimeException();
        }


    }
}