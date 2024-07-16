package Remoa.BE.Web.Member.Controller;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Comment.Service.CommentReplyService;
import Remoa.BE.Web.Comment.Service.CommentService;
import Remoa.BE.Web.CommentFeedback.Service.CommentFeedbackService;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Feedback.Service.FeedbackReplyService;
import Remoa.BE.Web.Feedback.Service.FeedbackService;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Member.Service.WithdrewService;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Repository.UploadFileRepository;
import Remoa.BE.Web.Post.Service.FileService;
import Remoa.BE.Web.Post.Service.PostService;
import Remoa.BE.config.auth.MemberDetails;
import Remoa.BE.exception.response.ErrorResponse;
import Remoa.BE.utill.MessageUtils;
import io.lettuce.core.event.command.CommandStartedEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Tag(name = "탈퇴 기능", description = "탈퇴 기능 API")
@RestController
@Slf4j
@RequiredArgsConstructor
public class WithdrewController {

    private final WithdrewService withdrewService;
    private final MemberService memberService;
    private final PostService postService;
    private final FeedbackService feedbackService;
    private final FeedbackReplyService feedbackReplyService;
    private final CommentService commentService;
    private final CommentReplyService commentReplyService;
    private final CommentFeedbackService commentFeedbackService;
    private final FileService fileService;

    /**
     * PathVariable을 이용한 회원 탈퇴 uri.
     * 로그인 된 사용자인지, 해당 사용자의 탈퇴 요청이 맞는지 확인 후 탈퇴 처리.
     *
     * @return 로그인 되지 않은 상태면 403(forbidden), 다른 id 값을 통한 잘못된 요청을 하면 401(Unauthorized), 올바른 탈퇴 요청이면 200(OK)
     */

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인한 사용자가 자신의 계정을 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/remove")
    @Operation(summary = "회원 DB 제거 ", description = "데이터 베이스 영구삭제 (개발 전용)")
    public ResponseEntity<String> withdrewRemoa(@AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Delete /remove");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);

        List<Post> postsByMember = postService.findPostsByMember(myMember);
        postsByMember.forEach(post -> {
            List<Comment> commentsByPost = commentService.findCommentsByPost(post); //코멘트 조회
            commentsByPost.forEach(commentReplyService::deleteByComment); //코멘트 대댓글 삭제
            commentsByPost.forEach(commentService::deleteCommentLikeByComment); // 코멘트 좋아요 삭제
            commentsByPost.forEach(commentFeedbackService::deleteByComment); //코멘트-피드백 삭제
            commentService.deleteByPost(post); // 코멘트 삭제

            List<FeedbackMemberLog> feedbackMemberLogByPost = feedbackService.findFeedbackMemberLogByPost(post);//피드백로그 조회
            feedbackMemberLogByPost.forEach(feedbackReplyService::deleteByFeedbackMemberLog); //피드백 대댓글 삭제
            feedbackMemberLogByPost.forEach(feedbackService::deleteFeedbackLikeByFeedBack); //피드백 좋아요
            feedbackService.deleteFeedbackLogByPost(post);//피드백 로그 삭제

            List<Feedback> feedbackByPost = feedbackService.findFeedbackByPost(post); //피드백 조회
            feedbackByPost.forEach(commentFeedbackService::deleteByFeedback); //코멘트-피드백 삭제
            feedbackService.deleteFeedbackByPost(post); //피드백 삭제

            fileService.deleteByPost(post);
        });
        postService.deleteByMember(myMember); //해당 포스트 삭제

        commentFeedbackService.deleteByMember(myMember); //코멘트-피드백 삭제
        commentService.deleteByMember(myMember);
        commentReplyService.deleteByMember(myMember);
        feedbackService.deleteFeedbackByMember(myMember);
        feedbackService.deleteFeedbackLogByMember(myMember);
        feedbackReplyService.deleteByMember(myMember);

        //데이터베이스 영구 삭제
        memberService.deleteMemberFromDB(myMember);


        return new ResponseEntity<>("회원 탈퇴가 완료되었습니다.", HttpStatus.OK);
    }

    /**
     * Session 정보만을 활용해서 PathVariable 없이 구현한 회원 탈퇴 uri. 로그인 된 사용자인지만 확인하면 됨.
     *
     * @return 로그인 되지 않은 상태면 403(forbidden), 올바른 탈퇴 요청이면 200(OK)
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인한 사용자가 자신의 계정을 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/delete")
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자가 자신의 계정을 탈퇴합니다.")
    public ResponseEntity<String> withdrewRemoaWithoutPathVariable(@AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Delete /delete");

        Member findMember = memberService.findOne(memberDetails.getMemberId());

        withdrewService.withdrewRemoa(findMember);

        return new ResponseEntity<>("회원 탈퇴가 완료되었습니다.", HttpStatus.OK);
    }

}
