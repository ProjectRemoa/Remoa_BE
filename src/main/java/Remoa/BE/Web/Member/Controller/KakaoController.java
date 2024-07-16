package Remoa.BE.Web.Member.Controller;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Req.ReqSignupDto;
import Remoa.BE.Web.Member.Dto.Res.KakaoLoginResponseDto;
import Remoa.BE.Web.Member.Dto.Res.ResSignupDto;
import Remoa.BE.Web.Member.Service.KakaoService;
import Remoa.BE.Web.Member.Service.MemberService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Random;

import static Remoa.BE.utill.MemberInfo.securityLoginWithoutLoginForm;


@Tag(name = "kakao", description = "카카오 로그인 API")
@RestController
@Slf4j
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;
    private final MemberService memberService;

    /**
     * 카카오 로그인을 통해 code를 query string으로 받아오면, 코드를 통해 토큰, 토큰을 통해 사용자 정보를 얻어와 db에 해당 사용자가 존재하는지 여부를
     * 파악해 존재할 때는 로그인, 없을 땐 회원가입 페이지로 넘어가게 해줌.
     */
    // 프론트에서 인가코드 받아오는 url
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기존 회원 로그인"),
            @ApiResponse(responseCode = "201", description = "첫 로그인 회원가입"),
            @ApiResponse(responseCode = "400", description = MessageUtils.ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/login/kakao")
    @Operation(summary = "카카오 로그인", description = "카카오 로그인을 통해 사용자를 식별하고 로그인 또는 회원가입 처리합니다.")
    public ResponseEntity<BaseResponse<KakaoLoginResponseDto>> getCI(@RequestParam String code) throws IOException {
        log.info("EndPoint Get /login/kakao");

        log.info("code = " + code);
        KakaoLoginResponseDto kakaoLoginResponseDto = kakaoService.kakaoLogin(code);

        if(kakaoLoginResponseDto.isSignup()){
            BaseResponse<KakaoLoginResponseDto> response = new BaseResponse<>(CustomMessage.OK, kakaoLoginResponseDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        BaseResponse<KakaoLoginResponseDto> response = new BaseResponse<>(CustomMessage.OK, kakaoLoginResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
//        return SuccessResponse.<KakaoLoginResponseDto>builder()
//                .message(CustomMessage.OK.getMessage())
//                .detail(CustomMessage.OK.getDetail())
//                .data(kakaoLoginResponseDto)
//                .build();
    }

   /* *//**
     * front-end에서 회원가입에 필요한 정보를 넘겨주면 KakaoSignupForm으로 받아 회원가입을 진행시켜줌
     *//*
    @PostMapping("/signup/kakao")
    @Operation(summary = "카카오 회원가입", description = "카카오에서 제공하는 사용자 정보를 이용하여 회원가입을 진행합니다.")
    public ResponseEntity<BaseResponse<ResSignupDto>> signupKakaoMember(@RequestBody @Validated ReqSignupDto form, HttpServletRequest request) throws IOException {

        Member member = new Member();
        Random random = new Random();

        //닉네임 사용 가능하면 그대로 진행, 불가능하면 임의 닉네임 "유저-{randomInt}로 지정.
        String randomNumber = Integer.toString((random.nextInt(900_000) + 100_000)); // 100_000 ~ 999_999
        boolean nicknameDuplicate = memberService.isNicknameDuplicate("유저-" + randomNumber);
        while (nicknameDuplicate) { //특수문자는 닉네임에 사용할 수 없으나 임의로 지정하는 닉네임에는 사용 가능하게 해서 또 다른 중복 문제 없게끔.
            randomNumber = Integer.toString((random.nextInt(900_000) + 100_000)); // 100_000 ~ 999_999
            nicknameDuplicate = memberService.isNicknameDuplicate("유저-" + randomNumber);
        }
        member.setNickname("유저-" + randomNumber);


        //카카오에서 받은 프로필 사진 url 링크를 토대로 s3에 저장
        if (memberService.findByKakaoId(form.getKakaoId()).isPresent()) {
            throw new BaseException(CustomMessage.VALIDATED);
            //return failResponse(CustomMessage.VALIDATED, "kakaoId가 이미 가입되어 있습니다.");
        }
        member.setKakaoId(form.getKakaoId());
        member.setEmail(form.getEmail());
        member.setTermConsent(form.getTermConsent());

        memberService.join(member);
        securityLoginWithoutLoginForm(member);

        ResSignupDto result = ResSignupDto.builder().
                kakaoId(member.getKakaoId()).
                email(member.getEmail()).
                nickname(member.getNickname()).
                profileImage(member.getProfileImage()).
                termConsent(member.getTermConsent()).
                build();

        BaseResponse<ResSignupDto> response = new BaseResponse<>(CustomMessage.OK, result);
        return ResponseEntity.ok().body(response);
    }

    *//**
     * 로그아웃 기능
     * 세션무효화, jsession쿠키를 제거,
     *//*
    @PostMapping("/user/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리합니다.")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();

        return new ResponseEntity<>(HttpStatus.OK);

    }*/


}