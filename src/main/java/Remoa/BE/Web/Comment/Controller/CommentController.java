package Remoa.BE.Web.Comment.Controller;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Comment.Dto.Req.ReqCommentDto;
import Remoa.BE.Web.Comment.Dto.Res.ResCommentDto;
import Remoa.BE.Web.Comment.Dto.Res.ResCommentLikeDto;
import Remoa.BE.Web.Comment.Service.CommentService;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.MemberUtils;
import Remoa.BE.Web.Member.Service.MemberService;
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

@Tag(name = "레퍼런스 코멘트 기능 Test completed", description = "레퍼런스 코멘트 기능 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final MemberService memberService;
    private final CommentService commentService;
    private final MemberUtils memberUtils;

    //코멘트 작성
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코멘트을 성공적으로 등록했습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reference/{reference_id}/comment")
    @Operation(summary = "코멘트 작성 Test Completed", description = "코멘트를 작성합니다.")
    public ResponseEntity<BaseResponse<List<ResCommentDto>>> registerComment(@RequestBody ReqCommentDto req,
                                                                             @PathVariable("reference_id") Long postId,
                                                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint POST /reference/{reference_id}/comment");

        String content = req.getComment();
        Member myMember = memberService.findOne(memberDetails.getMemberId());
        commentService.registerComment(myMember, content, postId);

        // 조회한 post의 comment 조회 및 각 comment에 대한 commentReply 조회 -> 이후 ResCommentDto로 매핑
        List<ResCommentDto> resCommentDtos = memberUtils.commentList(postId, myMember);

        BaseResponse<List<ResCommentDto>> response = new BaseResponse<>(CustomMessage.OK, resCommentDtos);
        return ResponseEntity.ok(response);
    }


    // 코멘트 수정
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코멘트을 성공적으로 수정했습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/reference/comment/{comment_id}")
    @Operation(summary = "코멘트 수정 Test Completed", description = "작성한 코멘트를 수정합니다.")
    public ResponseEntity<BaseResponse<List<ResCommentDto>>> modifyComment(@RequestBody ReqCommentDto req,
                                                                           @PathVariable("comment_id") Long commentId,
                                                                           @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint PUT /reference/comment/{comment_id}");

        String content = req.getComment();
        Comment c = commentService.findOne(commentId);

        if (!Objects.equals(c.getMember().getMemberId(), memberDetails.getMemberId())) {
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }
        commentService.modifyComment(content, commentId); //내용 변경

        Member myMember = memberService.findOne(memberDetails.getMemberId());
        // 조회한 post의 comment 조회 및 각 comment에 대한 commentReply 조회 -> 이후 ResCommentDto로 매핑
        List<ResCommentDto> resCommentDtos = memberUtils.commentList(c.getPost().getPostId(), myMember);

        BaseResponse<List<ResCommentDto>> response = new BaseResponse<>(CustomMessage.OK, resCommentDtos);
        return ResponseEntity.ok(response);
    }

    //코멘트 삭제
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코멘트를 성공적으로 삭제했습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/reference/comment/{comment_id}")
    @Operation(summary = "코멘트 삭제 Test Completed", description = "작성한 코멘트를 삭제합니다.")
    public ResponseEntity<BaseResponse<List<ResCommentDto>>> deleteComment(@PathVariable("comment_id") Long commentId,
                                                                           @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Delete /reference/comment/{comment_id}");

        Comment c = commentService.findOne(commentId);

        if (!Objects.equals(c.getMember().getMemberId(), memberDetails.getMemberId())) {
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }
        commentService.deleteComment(commentId);

        Member myMember = memberService.findOne(memberDetails.getMemberId());
        // 조회한 post의 comment 조회 및 각 comment에 대한 commentReply 조회 -> 이후 ResCommentDto로 매핑
        List<ResCommentDto> resCommentDtos = memberUtils.commentList(c.getPost().getPostId(), myMember);

        BaseResponse<List<ResCommentDto>> response = new BaseResponse<>(CustomMessage.OK, resCommentDtos);
        return ResponseEntity.ok(response);
    }

    // 코멘트 좋아요
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코멘트에 성공적으로 좋아요를 눌렀습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reference/comment/{comment_id}/like") // 코멘트 좋아요
    @Operation(summary = "코멘트 좋아요 Test Completed", description = "코멘트에 좋아요를 누릅니다.")
    public ResponseEntity<ResCommentLikeDto> likeComment(@PathVariable("comment_id") Long commentId,
                                              @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Post /reference/comment/{comment_id}/like");

        Long memberId = memberDetails.getMemberId();
        Member member = memberService.findOne(memberId);
        commentService.likeComment(member, commentId);
        int count = commentService.commentLikeCount(commentId);
        ResCommentLikeDto responseDto = new ResCommentLikeDto(count);
        return ResponseEntity.ok(responseDto);
    }

}