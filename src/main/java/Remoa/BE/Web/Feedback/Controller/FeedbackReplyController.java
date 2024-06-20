package Remoa.BE.Web.Feedback.Controller;

import Remoa.BE.Web.Feedback.Domain.FeedbackReply;
import Remoa.BE.Web.Feedback.Dto.ResFeedbackDto2;
import Remoa.BE.Web.Feedback.Dto.ResFeedbackLikeDto;
import Remoa.BE.Web.Feedback.Dto.ResFeedbackReplyLikeDto;
import Remoa.BE.Web.Feedback.Service.FeedbackReplyService;
import Remoa.BE.Web.Feedback.Service.FeedbackService;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.MemberUtils;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Post.Dto.Request.ReqFeedbackDto;
import Remoa.BE.Web.Post.Dto.Request.ReqFeedbackReplyDto;
import Remoa.BE.Web.Post.Service.PostService;
import Remoa.BE.config.auth.MemberDetails;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import Remoa.BE.exception.response.BaseResponse;
import Remoa.BE.exception.response.ErrorResponse;
import Remoa.BE.utill.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Tag(name = "레퍼런스 피드백 대댓글 기능 Test Completed", description = "레퍼런스 피드백 대댓글 기능 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class FeedbackReplyController {


    private final MemberService memberService;
    private final FeedbackReplyService feedbackReplyService;
    private final MemberUtils memberUtils;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "피드백 대댓글을 성공적으로 등록했습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reference/{reference_id}/feedback/{feedback_id}") // 레퍼런스에 피드백 대댓글 등록
    @Operation(summary = "피드백 대댓글 등록 Test Completed", description = "특정 피드백에 대댓글을 등록합니다.")
    public ResponseEntity<BaseResponse<List<ResFeedbackDto2>>> registerFeedbackReply(@RequestBody ReqFeedbackReplyDto req,
                                                                                     @PathVariable("reference_id") Long postId,
                                                                                     @PathVariable("feedback_id") Long feedbackId,
                                                                                     @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Post /reference/{reference_id}/feedback/{feedback_id}");

        String content = req.getFeedbackReply();
        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);
        feedbackReplyService.registerFeedbackReply(myMember, postId, feedbackId, content);

        // 조회한 post의 feedback 조회 및 각 feedback에 대한 feedbackReply 조회 -> 이후 ResFeedbackDto로 매핑
        List<ResFeedbackDto2> resFeedbackDtos = memberUtils.feedbackList(postId, myMember);

        return ResponseEntity.ok(new BaseResponse<>(CustomMessage.OK, resFeedbackDtos));
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "피드백 대댓글을 성공적으로 수정했습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/reference/feedback/{feedback_id}/reply/{reply_id}") // 피드백 대댓글 수정
    @Operation(summary = "피드백 대댓글 수정 Test Completed", description = "작성한 피드백 대댓글을 수정합니다.")
    public ResponseEntity<BaseResponse<List<ResFeedbackDto2>>> modifyFeedbackReply(@RequestBody ReqFeedbackReplyDto req,
                                                                                   @PathVariable("feedback_id") Long feedbackId,
                                                                                   @PathVariable("reply_id") Long replyId,
                                                                                   @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Put /reference/feedback/{feedback_id}/reply/{reply_id}");

        String content = req.getFeedbackReply();
        FeedbackReply reply = feedbackReplyService.findOne(replyId);
        Long myMemberId = memberDetails.getMemberId();
        if (!Objects.equals(reply.getMember().getMemberId(), myMemberId)) {
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }

        feedbackReplyService.modifyFeedbackReply(content, replyId);

        Member myMember = memberService.findOne(myMemberId);
        // 조회한 post의 feedback 조회 및 각 feedback에 대한 feedbackReply 조회 -> 이후 ResFeedbackDto로 매핑
        List<ResFeedbackDto2> resFeedbackDtos = memberUtils.feedbackList(reply.getPost().getPostId(), myMember);


        return ResponseEntity.ok(new BaseResponse<>(CustomMessage.OK, resFeedbackDtos));
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "피드백 대댓글을 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/reference/feedback/{feedback_id}/reply/{reply_id}") // 피드백 대댓글 삭제
    @Operation(summary = "피드백 대댓글 삭제 Test Completed", description = "작성한 피드백 대댓글을 삭제합니다.")
    public ResponseEntity<BaseResponse<List<ResFeedbackDto2>>> deleteFeedbackReply(@PathVariable("feedback_id") Long feedbackId,
                                                                                   @PathVariable("reply_id") Long replyId,
                                                                                   @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Delete /reference/feedback/{feedback_id}/reply/{reply_id}");

        FeedbackReply reply = feedbackReplyService.findOne(replyId);

        Long myMemberId = memberDetails.getMemberId();
        if (!Objects.equals(reply.getMember().getMemberId(), myMemberId)) {
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }

        feedbackReplyService.deleteFeedbackReply(replyId);

        Member myMember = memberService.findOne(myMemberId);
        // 조회한 post의 feedback 조회 및 각 feedback에 대한 feedbackReply 조회 -> 이후 ResFeedbackDto로 매핑
        List<ResFeedbackDto2> resFeedbackDto2s = memberUtils.feedbackList(reply.getPost().getPostId(), myMember);


        return ResponseEntity.ok(new BaseResponse<>(CustomMessage.OK, resFeedbackDto2s));
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "피드백 대댓글에 좋아요를 성공적으로 등록했습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reference/feedback_reply/{reply_id}/like") // 피드백 좋아요
    @Operation(summary = "피드백 대댓글 좋아요", description = "피드백 대댓글에 좋아요를 누릅니다.")
    public ResponseEntity<BaseResponse<ResFeedbackReplyLikeDto>> likeFeedback(@PathVariable("reply_id") Long replyId,
                                                                              @AuthenticationPrincipal MemberDetails memberDetails) {

        log.info("EndPoint Post /reference/feedback/{feedback_id}/like");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);
        feedbackReplyService.likeFeedbackReply(myMember, replyId);
        int count = feedbackReplyService.feedbackReplyLikeCount(replyId);
        ResFeedbackReplyLikeDto dto = new ResFeedbackReplyLikeDto(count);
        return ResponseEntity.ok(new BaseResponse<>(CustomMessage.OK, dto));
    }


}
