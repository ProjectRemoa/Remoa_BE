package Remoa.BE.Web.Notice.Controller;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Notice.Dto.Req.ReqNoticeDto;
import Remoa.BE.Web.Notice.Dto.Res.NoticeResponseDto;
import Remoa.BE.Web.Notice.Dto.Res.ResNoticeDetailDto;
import Remoa.BE.Web.Notice.Service.NoticeService;
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
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공지 기능 Test Completed", description = "공지 기능 API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;
    private final MemberService memberService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지가 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = MessageUtils.FORBIDDEN,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/notice")
    @Operation(summary = "공지 등록 Test Completed", description = "공지를 등록합니다.")
    public ResponseEntity<Void> postNotice(@Validated @RequestBody ReqNoticeDto reqNoticeDto,
                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Post /notice");

        Member myMember = memberService.findOne(memberDetails.getMemberId());
        noticeService.registerNotice(reqNoticeDto, myMember.getNickname());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = MessageUtils.FORBIDDEN,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/notice/{id}")
    @Operation(summary = "공지 수정 Test Completed", description = "공지를 수정합니다.")
    public ResponseEntity<Void> updateNotice(@PathVariable("id") Long noticeId,
                                               @Validated @RequestBody ReqNoticeDto reqNoticeDto,
                                               @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Put /notice/{id}");

        Member myMember = memberService.findOne(memberDetails.getMemberId());
        noticeService.updateNotice(noticeId, reqNoticeDto, myMember.getNickname());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = MessageUtils.FORBIDDEN,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/notice/{id}")
    @Operation(summary = "공지 삭제 Test Completed", description = "공지를 삭제합니다.")
    public ResponseEntity<Void> deleteNotice(@PathVariable("id") Long noticeId,
                                               @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Delete /notice/{id}");

        Member myMember = memberService.findOne(memberDetails.getMemberId());
        noticeService.deleteNotice(noticeId, myMember);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/notice")
    @Operation(summary = "공지 목록 조회 Test Completed", description = "페이지별 공지 목록을 조회합니다.")
    public ResponseEntity<BaseResponse<NoticeResponseDto>> getNotice(@RequestParam(required = false, defaultValue = "1", name = "page") int pageNumber) {
        log.info("EndPoint Get /notice");

        pageNumber -= 1;
        if (pageNumber < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }
        BaseResponse<NoticeResponseDto> response = new BaseResponse<>(CustomMessage.OK, noticeService.getNotice(pageNumber));
        return ResponseEntity.ok(response);
        //return successResponse(CustomMessage.OK, noticeService.getNotice(pageNumber));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지 상세를 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    @GetMapping("/notice/view")
    @Operation(summary = "공지 상세 조회 Test Completed", description = "특정 공지의 상세 정보를 조회합니다.")
    public ResponseEntity<BaseResponse<ResNoticeDetailDto>> getNoticeDetail(@RequestParam("view") int noticeId,
                                                                         HttpSession session) {
        log.info("EndPoint Get /notice/view");


        BaseResponse<ResNoticeDetailDto> response = new BaseResponse<>(CustomMessage.OK, noticeService.getNoticeView(noticeId,session));
        return ResponseEntity.ok(response);
        // return successResponse(CustomMessage.OK, noticeService.getNoticeView(view));
    }
}
