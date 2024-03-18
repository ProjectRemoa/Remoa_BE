package Remoa.BE.Member.Controller;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Dto.Res.ResMypageFollowing;
import Remoa.BE.Member.Dto.Res.ResMypageList;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Member.Service.MyFollowingService;
import Remoa.BE.exception.CustomMessage;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static Remoa.BE.exception.CustomBody.errorResponse;
import static Remoa.BE.exception.CustomBody.successResponse;
import static Remoa.BE.utill.MemberInfo.authorized;
import static Remoa.BE.utill.MemberInfo.getMemberId;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MyFollowingController {

    private final MemberService memberService;

    private final MyFollowingService myFollowingService;

    @GetMapping("/following") // 마이페이지 팔로잉 관리화면
    @Operation(summary = "마이페이지 팔로잉 관리화면", description = "현재 로그인된 사용자의 팔로잉 목록을 조회합니다.")
    public ResponseEntity<Object> mypageFollowing(HttpServletRequest request){
        if(authorized(request)){
            Long myMemberId = getMemberId();
            Member myMember = memberService.findOne(myMemberId);
            ResMypageFollowing resMypageFollowing = myFollowingService.mypageFollowing(myMember);

            return successResponse(CustomMessage.OK, resMypageFollowing);
        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }

    @GetMapping("/follower") // 마이페이지 팔로워 관리화면
    @Operation(summary = "마이페이지 팔로워 관리화면", description = "현재 로그인된 사용자의 팔로워 목록을 조회합니다.")
    public ResponseEntity<Object> mypageFollower(HttpServletRequest request){
        if(authorized(request)){
            Long myMemberId = getMemberId();
            Member myMember = memberService.findOne(myMemberId);
            ResMypageFollowing resMypageFollower = myFollowingService.mypageFollower(myMember);

            return successResponse(CustomMessage.OK, resMypageFollower);
        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }

}
