package Remoa.BE.Web.Member.Dto.GerneralLoginDto;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Res.RemoaToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "일반 테스트 로그인 응답")
public class GeneralLoginRes {

    @Schema(description = "토큰 정보")
    RemoaToken remoaToken;

    @Schema(description = "회원 닉네임", example = "testNickname1")
    String nickname;

    @Schema(description = "회원 이름", example = "김김김")
    String name;

    @Schema(description = "회원 번호", example = "1")
    Long memberId;

    @Schema(description = "회원 역할", example = "USER")
    String role;


    public GeneralLoginRes(String accessToken, String refreshToken, Member member) {
        this.remoaToken = new RemoaToken(accessToken, refreshToken);
        this.nickname = member.getNickname();
        this.name = member.getName();
        this.memberId = member.getMemberId();
        this.role = member.getRole().toString();
    }
}
