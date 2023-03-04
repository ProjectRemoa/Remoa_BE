package Remoa.BE.Member.Controller;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Dto.Req.EditProfileForm;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Member.Service.ProfileService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static Remoa.BE.exception.CustomBody.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    private final MemberService memberService;

    // 프로필 수정 범위 : 닉네임(중복확인), 핸드폰번호, 대학교, 한줄소개
    @GetMapping("/user")
    public ResponseEntity<Object> userHome(HttpServletRequest request) {

        HttpSession session = request.getSession();
        // 현재 로그인한 사용자의 세션 가져오기
        Member loginMember = (Member) session.getAttribute("loginMember");

        // 세션이 없으면 로그인 페이지로 이동
        if (loginMember == null) {
            return failResponse(CustomMessage.VALIDATED, "redirect:/login/kakao");
        }

        // 로그인된 사용자의 정보를 db에서 다시 불러와 띄워줌.
        Member member = memberService.findOne(loginMember.getMemberId());
        return successResponse(CustomMessage.OK, member);
    }

    // RESTful API에서 PUT 매핑은 수정할 리소스를 명확하게 지정해야 하는데 이 경우에는 URL에 리소스 ID를 명시하는 것이 일반적이다.
    // 그런데 우리는 수정할 사용자의 정보를 모두 입력받아 수정하는 형태이기 때문에
    // URL에 리소스 ID를 명시할 필요가 없어서 PUT대신 POST 매핑을 사용하였습니다.
    @PostMapping("/user")
    public ResponseEntity<Object> editProfile(@RequestBody EditProfileForm form, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Member loginMember = (Member) session.getAttribute("loginMember");

        if (loginMember == null) {
            // 로그인되어 있지 않은 경우 로그인 페이지로 이동
            return failResponse(CustomMessage.VALIDATED, "로그인하지 않은 회원입니다. <redirect:/login/kakao> 로 redirect");
        }

        if (memberService.isNicknameDuplicate(form.getNickname()) &&
                isNicknameChanged(form, loginMember)) {
            return failResponse(CustomMessage.VALIDATED, "닉네임이 중복됩니다.");
        }

        // 사용자의 입력 정보를 DTO에 담아 서비스로 전달
        EditProfileForm profileInfo = new EditProfileForm(
                form.getNickname(), form.getPhoneNumber(), form.getUniversity(), form.getOneLineIntroduction());
        profileService.editProfile(loginMember.getMemberId(), profileInfo);

        // 수정이 완료되면 프로필 페이지로 이동
        return successResponse(CustomMessage.OK, "redirect:/user");
    }

    private boolean isNicknameChanged(EditProfileForm form, Member loginMember) {
        return !(form.getNickname().equals(loginMember.getNickname()));
    }

    /**
     * 프론트에서 닉네임 중복 검사를 할 때 사용할 메서드
     * @param nickname
     * @return ResponseEntity
     */
    @GetMapping("/nickname")
    public ResponseEntity<Object> checkNicknameDuplicate(@RequestParam String nickname) {
        if (memberService.isNicknameDuplicate(nickname)) {
            return successResponse(CustomMessage.OK, nickname + "은(는) 이미 사용중인 닉네임입니다.");
        } else {
            return successResponse(CustomMessage.OK, nickname + "은(는) 사용 가능한 닉네임입니다.");
        }
    }
}