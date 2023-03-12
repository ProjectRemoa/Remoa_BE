package Remoa.BE.Post.Controller;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.UploadFile;
import Remoa.BE.Post.Dto.Response.ResRegisterCommentDto;
import Remoa.BE.Post.Dto.Response.ThumbnailReferenceDto;
import Remoa.BE.Post.Service.CommentService;
import Remoa.BE.Post.Service.MyPostService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static Remoa.BE.exception.CustomBody.errorResponse;
import static Remoa.BE.exception.CustomBody.successResponse;
import static Remoa.BE.utill.MemberInfo.authorized;
import static Remoa.BE.utill.MemberInfo.getMemberId;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyPostController {

    private final MemberService memberService;
    private final CommentService commentService;
    private final MyPostService myPostService;

    // Entity <-> DTO 간의 변환을 편리하게 하고자 ModelMapper 사용.(build.gradle에 의존성 주입 완료)
    private final ModelMapper modelMapper = new ModelMapper();

    @PostMapping("/reference/{reference_id}/comment")
    public ResponseEntity<Object> registerComment(@RequestParam Map<String, String> comment,
                                                @PathVariable("reference_id") Long postId,
                                                HttpServletRequest request) {
        String myComment = comment.get("comment");
        if (authorized(request)) {
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);
            ResRegisterCommentDto resRegisterCommentDto = commentService.registerComment(myMember, myComment, postId);
            return successResponse(CustomMessage.OK, resRegisterCommentDto);
        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }

    /**
     * 내 작업물 목록 페이지
     * @return ThumbnailDto -> 레퍼런스 대표 thumbnail 이미지는 uploadFiles.get(0)에 적힌 URI 참고
     */
    @GetMapping("/user/reference")
    public ResponseEntity<Object> myReferences(HttpServletRequest request) {

        if (authorized(request)) {
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);

            List<Post> allPosts = myPostService.showOnesPosts(myMember);
            List<ThumbnailReferenceDto> myReferenceList = new ArrayList<>();

            // ModelMapper initialize
            initModelMapper();

            for (Post post : allPosts) {
                // ModelMapper 통해 Entity -> DTO 변환
                ThumbnailReferenceDto postDTO = modelMapper.map(post, ThumbnailReferenceDto.class);

                myReferenceList.add(postDTO);
            }
            return successResponse(CustomMessage.OK, myReferenceList);
        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }

    /**
     * Post Entity -> Thumbnail용 Response DTO를 위한 ModelMapper.
     */
    private void initModelMapper() {
        modelMapper.typeMap(Post.class, ThumbnailReferenceDto.class)
                .addMappings(mapper -> mapper.using(
                                (Converter<Member, String>) context -> context.getSource().getNickname())
                        .map(Post::getMember, ThumbnailReferenceDto::setNickname))
                .addMappings(mapper -> mapper.using(
                        (Converter<List<UploadFile>, List<String>>) context -> context.getSource().stream()
                                .map(UploadFile::getStoreFileUrl).collect(Collectors.toList()))
                        .map(Post::getUploadFiles, ThumbnailReferenceDto::setStoreFileUrls));
    }

}
