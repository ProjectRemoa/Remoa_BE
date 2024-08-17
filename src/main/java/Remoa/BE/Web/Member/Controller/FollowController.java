package Remoa.BE.Web.Member.Controller;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Res.ResFollowerAndFollowingDto;
import Remoa.BE.Web.Member.Service.FollowService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Tag(name = "팔로우 기능 Test Completed", description = "팔로우 기능 API")
@RestController
@Slf4j
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final MemberService memberService;

    /**
     * Session에서 로그인한 사용자(Follow를 거는 사용자)의 Member와 Follow 받는 대상의 memberId를 통해
     * Follow가 이미 되었는지 확인하고, 안 되어 있으면 Follow, 되어있다면 Unfollow를 할 수 있도록 기능합니다.
     *
     * @param toMemberId
     * @param memberDetails
     * @return ResponseEntity<String>
     */

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "언팔로우 성공적으로 수행되었습니다."),
            @ApiResponse(responseCode = "201", description = "팔로우가 성공적으로 수행되었습니다."),
            @ApiResponse(responseCode = "400", description = "자기 자신을 팔로우하는 경우입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/follow/{member_id}")
    @Operation(summary = "팔로우 기능 Test completed", description = "특정 회원을 팔로우하거나 언팔로우합니다.")
    public ResponseEntity<?> follow(@PathVariable("member_id") Long toMemberId,
                                    @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Post /follow/{member_id}");

        //나 자신을 팔로우 하는 경우
        Long myMemberId = memberDetails.getMemberId();
        if (Objects.equals(myMemberId, toMemberId)) {
            throw new BaseException(CustomMessage.SELF_FOLLOW);
            // return errorResponse(CustomMessage.SELF_FOLLOW);
        } else {
            boolean check = followService.followFunction(myMemberId, toMemberId);
            //팔로우
            if (check) {
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
            //언팔로우
            else {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
    }

    /**
     * @return ResponseEntity<Object>
     */

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로워 및 팔로잉 수 성공적 조회"),
    })
    @GetMapping("/follow/{member_id}")
    @Operation(summary = "팔로워 및 팔로잉 조회 Test completed", description = "특정 회원의 팔로워 및 팔로잉 수를 조회합니다.")
    public ResponseEntity<BaseResponse<ResFollowerAndFollowingDto>> showFollowers(@AuthenticationPrincipal MemberDetails memberDetails,
                                                                                  @PathVariable("member_id") Long memberId) {
        log.info("EndPoint Get /follow/{member_id}");

        Member myMember = null;

        if (memberDetails != null) {
            Long myMemberId = memberDetails.getMemberId();
            myMember = memberService.findOne(myMemberId);
        }

        Member member = memberService.findOne(memberId);
        List<Integer> count = followService.followerAndFollowing(member);
        ResFollowerAndFollowingDto result = ResFollowerAndFollowingDto.builder()
                .follower(count.get(0))
                .following(count.get(1))
                .isFollow(myMember != null ? followService.isMyMemberFollowMember(myMember, member) : null)
                .build();

        BaseResponse<ResFollowerAndFollowingDto> response = new BaseResponse<>(CustomMessage.OK, result);
        return ResponseEntity.ok(response);
        //   return successResponse(CustomMessage.OK, result);
    }
}
