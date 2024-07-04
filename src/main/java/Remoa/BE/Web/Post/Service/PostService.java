package Remoa.BE.Web.Post.Service;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Post.Domain.Category;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Domain.PostLike;
import Remoa.BE.Web.Post.Domain.PostScrap;
import Remoa.BE.Web.Post.Dto.Request.UploadPostForm;
import Remoa.BE.Web.Post.Repository.*;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static Remoa.BE.utill.Constant.HOME_PAGE_SIZE;
import static Remoa.BE.utill.FileExtension.fileExtension;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {


    private final MemberService memberService;

    private final PostRepository postRepository;

    private final CategoryRepository categoryRepository;

    private final FileService fileService;

    private final PostPagingRepository postPagingRepository;

    private final PostScrapRepository postScrapRepository;
    private final PostLikeRepository postLikeRepository;


    public List<Post> findPostsByMember(Member member) {
        return postRepository.findByMember(member);
    }

    public Post findOne(Long postId) {
        Optional<Post> post = postRepository.findOne(postId);
        return post.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post not found"));
    }

    @Transactional
    public Post findOneViewPlus(Long postId, HttpSession session) {
        Post post = postRepository.findOne(postId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        handleViewCount(post, session);
        return post;
    }

    private void handleViewCount(Post post, HttpSession session) {
        Long postId = post.getPostId();
        String sessionKey = "PostViewed_" + postId;
        log.info("sessionKey = {}", sessionKey);

        if (session.getAttribute(sessionKey) == null) {
            post.addViewCount();
            session.setAttribute(sessionKey, true);
        }
    }

//    public int findScrapCount(Long postId){
//        Post findPost = findOne(postId);
//        return findPost.getPostScraps().size();
//    }

//    public int findLikeCount(Long postId){
//        Post findPost = findOne(postId);
//        return findPost.getPostLikes().size();
//    }

    @Transactional
    public Post likePost(Long memberId, Member myMember, Long referenceId) {
        Post post = findOne(referenceId);
        Integer postLikeCount = post.getLikeCount(); // 이 게시물을 좋아요한 수

        PostLike postLike = postLikeRepository.findByMemberMemberIdAndPostPostId(memberId, referenceId);
        if (postLike == null) {
            post.setLikeCount(postLikeCount + 1); // 좋아요 + 1
            PostLike postLikeObj = PostLike.createPostLike(myMember, post);
            postLikeRepository.save(postLikeObj);
        } else {
            post.setLikeCount(post.getLikeCount() - 1); // 좋아요 - 1
            postLikeRepository.deleteById(postLike.getPostLikeId());
        }
        return post;
    }

    @Transactional
    public Post registerPost(UploadPostForm uploadPostForm, MultipartFile thumbnail, List<MultipartFile> uploadFiles, Long memberId) throws IOException {

        Category category = categoryRepository.findByCategoryName(uploadPostForm.getCategory());

        Member member = memberService.findOne(memberId);

        Post post;

        // 비디오만 따로 처리
        if (category.getName().equals("video")) {
            if (uploadFiles != null) {
                throw new IOException();
            }
            post = Post.builder()
                    .title(uploadPostForm.getTitle())
                    .member(member)
                    .contestName(uploadPostForm.getContestName())
                    .category(category)
                    .youtubeLink(uploadPostForm.getYoutubeLink())
                    .contestAwardType(uploadPostForm.getContestAwardType())
                    .pageCount(1)
                    .postingTime(LocalDateTime.now())
                    .likeCount(0)
                    .views(0)
                    .scrapCount(0)
                    .deleted(false)
                    .build();
            fileService.saveUploadFiles(post, thumbnail, null);
        } else {
            if (uploadFiles == null) {
                throw new IOException();
            }
            //확장자 확인
            String extension = fileExtension(uploadFiles.get(0));
            if (extension.equals("pdf") || extension.equals("jpg") || extension.equals("png")) {
                int pageCount;
                if (extension.equals("pdf")) {
                    PDDocument document = PDDocument.load(uploadFiles.get(0).getInputStream());
                    pageCount = document.getNumberOfPages();
                } else {
                    pageCount = uploadFiles.size();
                }
                post = Post.builder()
                        .title(uploadPostForm.getTitle())
                        .member(member)
                        .contestName(uploadPostForm.getContestName())
                        .category(category)
                        .youtubeLink(uploadPostForm.getYoutubeLink())
                        .contestAwardType(uploadPostForm.getContestAwardType())
                        .pageCount(pageCount)
                        .postingTime(LocalDateTime.now())
                        .likeCount(0)
                        .views(0)
                        .scrapCount(0)
                        .deleted(false)
                        .build();

                fileService.saveUploadFiles(post, thumbnail, uploadFiles);
            } else {
                throw new IOException();
            }
        }

        return post;
    }

    @Transactional
    public Post modifyPost(UploadPostForm uploadPostForm, MultipartFile thumbnail, List<MultipartFile> uploadFiles, Post originPost) throws IOException {
        Category category = categoryRepository.findByCategoryName(uploadPostForm.getCategory());

        Member member = memberService.findOne(originPost.getMember().getMemberId());

        log.info("thumbnail = " + thumbnail.getOriginalFilename());

        // 비디오만 따로 처리
        if (category.getName().equals("video")) {
            if (uploadFiles != null) {
                throw new IOException();
            }

            originPost.setTitle(uploadPostForm.getTitle());
            originPost.setContestName(uploadPostForm.getContestName());
            originPost.setCategory(category);
            originPost.setYoutubeLink(uploadPostForm.getYoutubeLink());
            originPost.setContestAwardType(uploadPostForm.getContestAwardType());
            originPost.setPageCount(1);


            fileService.modifyUploadFiles(originPost, thumbnail, null);
        } else {
            if (uploadFiles == null) {
                throw new IOException();
            }
            //확장자 확인
            String extension = fileExtension(uploadFiles.get(0));
            if (extension.equals("pdf") || extension.equals("jpg") || extension.equals("png")) {
                int pageCount;
                if (extension.equals("pdf")) {
                    PDDocument document = PDDocument.load(uploadFiles.get(0).getInputStream());
                    pageCount = document.getNumberOfPages();
                } else {
                    pageCount = uploadFiles.size();
                }

                originPost.setTitle(uploadPostForm.getTitle());
                originPost.setContestName(uploadPostForm.getContestName());
                originPost.setCategory(category);
                originPost.setYoutubeLink(uploadPostForm.getYoutubeLink());
                originPost.setContestAwardType(uploadPostForm.getContestAwardType());
                originPost.setPageCount(pageCount);

                fileService.modifyUploadFiles(originPost, thumbnail, uploadFiles);
            } else {
                throw new IOException();
            }
        }
        return originPost;
    }

    public Page<Post> sortAndPaginatePosts(String sort, int pageNumber, String searchQuery) {
        Page<Post> Posts;
        Pageable pageable;
        switch (sort) {
            case "likes":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("likeCount").descending());
                Posts = postPagingRepository.findByMemberNameOrTitleContaining(pageable, searchQuery);
                break;
            case "scrap":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("scrapCount").descending());
                Posts = postPagingRepository.findByMemberNameOrTitleContaining(pageable, searchQuery);
                break;
            case "views":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("views").descending());
                Posts = postPagingRepository.findByMemberNameOrTitleContaining(pageable, searchQuery);
                break;
            default:
                //sort 문자열이 잘못됐을 경우 default인 최신순으로 정렬
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("postingTime").descending());
                Posts = postPagingRepository.findByMemberNameOrTitleContaining(pageable, searchQuery);
                break;
        }
        return Posts;
    }

    public Page<Post> sortAndPaginatePostsByCategory(String category, String sort, int pageNumber, String searchQuery) {
        Page<Post> Posts;
        Pageable pageable;
        switch (sort) {
            case "likes":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("likeCount").descending());
                break;
            case "scrap":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("scrapCount").descending());
                break;
            case "views":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("views").descending());
                break;
            default:
                //sort 문자열이 잘못됐을 경우 default인 최신순으로 정렬
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("postingTime").descending());
                break;
        }
        Posts = postPagingRepository.findByMemberNameOrTitleContainingAndCategory(pageable, searchQuery, categoryRepository.findByCategoryName(category));
        return Posts;
    }

    public PostScrap getPostScrapByMemberIdAndPostId(Long memberId, Long postId) {
        return postScrapRepository.findByMemberMemberIdAndPostPostId(memberId, postId);
    }

    @Transactional
    public boolean scrapPost(Long memberId, Member myMember, Long referenceId) {
        boolean isScrapAction = true;
        Post post = findOne(referenceId);
        Integer postScrapCount = post.getScrapCount(); // 이 게시물을 스크랩한 수

        // scrapPost를 db에서 조회해보고 조회 결과가 null이면 scrapCount += 1, PostScrap 생성
        // null이 아니면 scrapCount -= 1, 조회결과인 해당 PostScrap 삭제
        PostScrap postScrap = getPostScrapByMemberIdAndPostId(memberId, referenceId);
        if (postScrap == null) {
            post.setScrapCount(postScrapCount + 1); // 스크랩 수 1 증가
            PostScrap postScrapObj = PostScrap.createPostScrap(myMember, post);
            postScrapRepository.save(postScrapObj);
            isScrapAction = true;
        } else {
            post.setScrapCount(post.getScrapCount() - 1); // 스크랩 수 1 차감
            postScrapRepository.deleteById(postScrap.getPostScrapId()); // db에서 삭제
            isScrapAction = false;
        }
        return isScrapAction;
    }

    public Page<PostScrap> findScrapedPost(int page, Member member, String categoryString) {
        Category category = null;
        List<String> categoryList = Arrays.asList("idea", "marketing", "design", "video", "digital", "etc");
        if (categoryList.contains(categoryString)) {
            category = categoryRepository.findByCategoryName(categoryString);
        }
        Pageable pageable = PageRequest.of(page, HOME_PAGE_SIZE);
        return postScrapRepository.findMyScrapedPost(member, pageable, category);
    }

    public List<Post> findRecentTwelveScrapedPost(Member member) {
        return postPagingRepository.findByMemberRecentTwelve(member);
    }

    public boolean checkMemberPost(Member myMember, Long postId) {
        // postId를 이용해 Post 엔티티에 등록된 Member를 불러오고 이 Member가 컨트롤러에서 불러온 Member와 맞는지 확인하고 true/false 반환
        Post post = findOne(postId);
        Member member = post.getMember();
        return myMember == member;
    }

    public boolean isThisPostScraped(Member myMember, Post post) {
        return postRepository.findScrapedPost(myMember, post).isPresent();
    }

    public boolean isThisPostLiked(Member myMember, Post post) {
        return postRepository.findLikedPost(myMember, post).isPresent();
    }

    @Transactional
    public void deleteReference(Long postId) {
        postRepository.deletePost(postId);
    }

    public Member getPostedMember(Long postId) {
        return postRepository.findPostedMember(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post not found"));
    }
}