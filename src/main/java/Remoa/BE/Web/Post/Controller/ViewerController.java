package Remoa.BE.Web.Post.Controller;

import Remoa.BE.Web.Comment.Dto.Res.ResCommentDto;
import Remoa.BE.Web.Feedback.Dto.ResFeedbackDto2;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.MemberUtils;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Dto.Response.ResReferenceViewerDto;
import Remoa.BE.Web.Post.Service.PostService;
import Remoa.BE.config.auth.MemberDetails;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "레퍼런스 상세 기능 Test Completed", description = "레퍼런스 상세 기능 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ViewerController {

    private final MemberUtils memberUtils;
    private final PostService postService;
    private final MemberService memberService;


    @GetMapping("/reference/{reference_id}")
    @Operation(summary = "레퍼런스 조회 Test Completed", description = "특정 레퍼런스의 상세 정보를 조회합니다.")
    public ResponseEntity<BaseResponse<ResReferenceViewerDto>> referenceViewer(@PathVariable("reference_id") Long referenceId,
                                                                               @AuthenticationPrincipal MemberDetails memberDetails) {

        log.info("EndPoint Get /reference/{reference_id}");

        Long myMemberId;

        Member myMember = null;
        if (memberDetails != null) {
            myMemberId = memberDetails.getMemberId();
            myMember = memberService.findOne(myMemberId);
        }

        // query parameter로 넘어온 id값의 post 조회
        Post post = postService.findOneViewPlus(referenceId);

        // 조회한 post의 comment 조회 및 각 comment에 대한 commentReply 조회 -> 이후 ResCommentDto로 매핑
        List<ResCommentDto> comments = memberUtils.commentList(post.getPostId(), myMember);

        // 조회한 post의 feedback 조회 및 각 feedback에 대한 feedbackReply 조회 -> 이후 ResFeedbackDto로 매핑
        List<ResFeedbackDto2> feedbacks = memberUtils.feedbackList(post.getPostId(), myMember);

        // 위에 생성한 CommentDto, FeedbackDto를 이용해 ReferenceViewerDto 매핑.
        ResReferenceViewerDto resReferenceViewerDto = new ResReferenceViewerDto(post,
                post.getMember(),
                memberUtils.isMyMemberFollowMember(myMember, post.getMember()),
                memberUtils.isLikedPost(myMember, post),
                memberUtils.isScrapedPost(myMember, post),
                comments,
                feedbacks
        );

        BaseResponse<ResReferenceViewerDto> response = new BaseResponse<>(CustomMessage.OK, resReferenceViewerDto);
        return ResponseEntity.ok(response);
    }


}
