package Remoa.BE.Web.Member.Dto.Res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResReIssue {

    @Schema(description = "토큰 정보")
    RemoaToken remoaToken;

    public ResReIssue(String accessToken, String refreshToken) {
        remoaToken = new RemoaToken(accessToken, refreshToken);
    }
}