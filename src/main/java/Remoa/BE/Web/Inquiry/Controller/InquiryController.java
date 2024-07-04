package Remoa.BE.Web.Inquiry.Controller;

import Remoa.BE.Web.Inquiry.Dto.Req.ReqInquiryDto;
import Remoa.BE.Web.Inquiry.Dto.Res.ResInquiryDetailDto;
import Remoa.BE.Web.Inquiry.Dto.Res.ResInquiryPaging;
import Remoa.BE.Web.Inquiry.Service.InquiryService;
import Remoa.BE.Web.Member.Domain.Member;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "문의 기능 Test Completed", description = "문의 기능 API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class InquiryController {

    private final InquiryService inquiryService;
    private final MemberService memberService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의가 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/inquiry")
    @Operation(summary = "문의 등록 Test Completed", description = "문의를 등록합니다.")
    public ResponseEntity<Void> postInquiry(@Validated @RequestBody ReqInquiryDto inquiryDto,
                                            @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Post /inquiry");

        Member myMember = memberService.findOne(memberDetails.getMemberId());
        inquiryService.registerInquiry(inquiryDto, myMember.getNickname());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/inquiry/{id}")
    @Operation(summary = "문의 수정 Test Completed", description = "문의를 수정합니다.")
    public ResponseEntity<Void> updateInquiry(@PathVariable("id") Long inquiryId,
                                              @Validated @RequestBody ReqInquiryDto inquiryDto,
                                              @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Put /inquiry/{id}");

        Member myMember = memberService.findOne(memberDetails.getMemberId());
        inquiryService.updateInquiry(inquiryId, inquiryDto, myMember);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/inquiry/{id}")
    @Operation(summary = "문의 삭제 Test Completed", description = "문의를 삭제합니다.")
    public ResponseEntity<Void> deleteInquiry(@PathVariable("id") Long inquiryId,
                                              @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Put /inquiry/{id}");

        Member myMember = memberService.findOne(memberDetails.getMemberId());
        inquiryService.deleteInquiry(inquiryId, myMember);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 목록을 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/inquiry")
    @Operation(summary = "문의 목록 조회 Test Completed", description = "페이지별 문의 목록을 조회합니다.")
    public ResponseEntity<BaseResponse<ResInquiryPaging>> getInquiry(@RequestParam(required = false, defaultValue = "1", name = "page") int pageNumber) {
        log.info("EndPoint Get /inquiry");

        pageNumber -= 1;
        if (pageNumber < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //    return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        BaseResponse<ResInquiryPaging> response = new BaseResponse<>(CustomMessage.OK, inquiryService.getInquiry(pageNumber));
        return ResponseEntity.ok(response);
        // return successResponse(CustomMessage.OK, inquiryService.getInquiry(pageNumber));
    }


    /**
     * 리턴할 경우 문의 답변도 함께 리턴해야 하는지 결정해야 함
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 상세 정보를 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "해당 문의가 존재하지 않습니다.")
    })
    @GetMapping("/inquiry/view")
    @Operation(summary = "문의 상세 조회 Test Completed", description = "특정 문의의 상세 정보를 조회합니다.")
    public ResponseEntity<BaseResponse<ResInquiryDetailDto>> getInquiryDetail(@RequestParam("view") int inquiryId,
                                                                              HttpSession session) {
        log.info("EndPoint Get /inquiry/view");

        BaseResponse<ResInquiryDetailDto> response = new BaseResponse<>(CustomMessage.OK, inquiryService.getInquiryView(inquiryId, session));
        return ResponseEntity.ok(response);
    }


}