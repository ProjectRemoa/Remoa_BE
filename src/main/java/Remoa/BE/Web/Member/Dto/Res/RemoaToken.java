package Remoa.BE.Web.Member.Dto.Res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Schema(description = "인증 토큰 정보")
public class RemoaToken {

    @Schema(description = "JWT 인증 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QWNjb3VudCIsImFjY291bnQiOiJ0ZXN0QWNjb3VudCIsImlhdCI6MTcxMDIyMTI1MCwiZXhwIjoxNzEwODI2MDUwfQ.wpMIUytr8MpqxGpFAJIlF8kG9OSm2KJE7xeUWQHVnAU")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QWNjb3VudDEiLCJhY2NvdW50IjoidGVzdEFjY291bnQxIiwiaWF0IjoxNzE2MzY1MTY0LCJleHAiOjE3MTY0NTE1NjR9.Pl5iyo2TlZHqjfZmTpSgssZ_Gcz7ElnPtqq6PmzTlYY")
    private String refreshToken;

    // 필요한 생성자, getter, setter 등이 Lombok의 @Data와 @NoArgsConstructor에 의해 자동 생성됩니다.

    public RemoaToken(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
