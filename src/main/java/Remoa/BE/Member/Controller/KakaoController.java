package Remoa.BE.Member.Controller;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Dto.Req.ReqSignupDto;
import Remoa.BE.Member.Dto.Res.ResSignupDto;
import Remoa.BE.Member.Service.KakaoService;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static Remoa.BE.exception.CustomBody.*;
import static Remoa.BE.utill.MemberInfo.*;


@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 프론트에서 추가 요청
public class KakaoController {

    private final KakaoService ks;
    private final MemberService memberService;
    private final HttpSession httpSession;

    /**
     * 카카오 로그인을 통해 code를 query string으로 받아오면, 코드를 통해 토큰, 토큰을 통해 사용자 정보를 얻어와 db에 해당 사용자가 존재하는지 여부를
     * 파악해 존재할 때는 로그인, 없을 땐 회원가입 페이지로 넘어가게 해줌.
     */
    @GetMapping("/login/kakao")
    public ResponseEntity<Object> getCI(@RequestParam String code, HttpServletRequest request) throws IOException {
        log.info("code = " + code);

        // 액세스 토큰과 유저정보 받기
        String access_token = ks.getToken(code);
        Map<String, Object> userInfo = ks.getUserInfo(access_token);

        log.info("userInfo = {}", userInfo.values());

        Long kakaoId = Long.parseLong((String) userInfo.get("id"));
        Optional<Member> member = memberService.findByKakaoId(kakaoId);

        log.info("kakaoId = {}", kakaoId);

        /*
         * 백에서 보낼 게 없을 때에도 정상 처리됐다는 메세지 정도는 같이 보내주는 게 좋습니다.
         * 상태메세지는 body에, 상태코드는 head에 들어갑니다.
         * 에러메세지를 exception 패키지처럼 한곳에 모아놓고 쓰는 것도 좋습니다.
         */
        if (member.isPresent()) {
            securityLoginWithoutLoginForm(member.get(), request);
            //if문에 걸리지 않았다면 이미 회원가입이 진행돼 db에 kakaoId가 있는 유저이므로 kakaoMember가 존재하므로 LoginController처럼 로그인 처리 하면 됩니다.
            return successResponse(CustomMessage.OK, userInfo);

        } else {
            //kakaoId가 db에 없으므로 kakaoMember가 null이므로 회원가입하지 않은 회원. 따라서 회원가입이 필요하므로 회원가입하는 uri로 redirect 시켜주어야 함.
            return successResponse(CustomMessage.OK_SIGNUP, userInfo);
        }
    }

    /**
     * 카카오 로그인을 우회해 테스트 하기 위한 용도로 추가됨.
     * @param kakaoId
     * @return ResponseEntity
     */
    @PostMapping("/login/kakao/test")
    public ResponseEntity<Object> testLogin(@RequestBody Integer kakaoId, HttpServletRequest request) {
        log.warn("kakaoId = {}", kakaoId);
        Optional<Member> findMember = memberService.findByKakaoId(Long.valueOf(kakaoId));
        if (findMember.isPresent()) {
            Member member = findMember.get();
            securityLoginWithoutLoginForm(member, request);
            return successResponse(CustomMessage.OK, member);
        }
        return failResponse(CustomMessage.VALIDATED, "User Not Exist");
    }

    /**
     * front-end에서 회원가입에 필요한 정보를 넘겨주면 KakaoSignupForm으로 받아 회원가입을 진행시켜줌
     */
    @PostMapping("/signup/kakao")
    public ResponseEntity<Object> signupKakaoMember(@RequestBody @Validated ReqSignupDto form, HttpServletRequest request) {

        Member member = new Member();

        //닉네임 사용 가능하면 그대로 진행, 불가능하면 임의 닉네임 "유저-{kakaoId}로 지정.
        Boolean nicknameDuplicate = memberService.isNicknameDuplicate(form.getNickname());
        if (nicknameDuplicate) { //특수문자는 닉네임에 사용할 수 없으나 임의로 지정하는 닉네임에는 사용 가능하게 해서 또 다른 중복 문제 없게끔.
            member.setNickname("유저-" + form.getKakaoId());
        } else {
            member.setNickname(form.getNickname());
        }
        member.setKakaoId(form.getKakaoId());
        member.setEmail(form.getEmail());
        member.setProfileImage(form.getProfileImage());
        member.setTermConsent(form.getTermConsent());

        memberService.join(member);
        securityLoginWithoutLoginForm(member, request);

        ResSignupDto result =  ResSignupDto.builder().
                kakaoId(member.getKakaoId()).
                email(member.getEmail()).
                nickname(member.getNickname()).
                profileImage(member.getProfileImage()).
                termConsent(member.getTermConsent()).
                build();

        return successResponse(CustomMessage.OK,result);
    }

    /**
     *자동 로그인 추후에
     */
/*
    @GetMapping("/login")
    public ResponseEntity<Object> autoLogin(){
       Long kaKaoId = getKaKaoId();
       Optional<Member> member = memberService.findByKakaoId(kaKaoId);
       if(member.isPresent()){
           return successResponse(CustomMessage.OK,member);
       }
       else{
           return errorResponse(CustomMessage.UNAUTHORIZED);
       }

    }
*/

    /**
     * 로그아웃 기능
     세션무효화, jsession쿠키를 제거,
     */
    @PostMapping("/user/logout")
    public ResponseEntity<Object> logout(HttpServletResponse response){

        SecurityContextHolder.clearContext(); // 현재 SecurityContext를 제거합니다.
        httpSession.invalidate(); // HttpSession을 무효화합니다.

        Cookie myCookie = new Cookie("JSESSIONID", null);
        myCookie.setMaxAge(0); // 쿠키의 expiration 타임을 0으로 하여 없앤다.
        myCookie.setPath("/"); // 모든 경로에서 삭제 됬음을 알린다.
        response.addCookie(myCookie);
        return successResponse(CustomMessage.OK,myCookie);
    }


}