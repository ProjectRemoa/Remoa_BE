package Remoa.BE.config.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/*@RedisHash(value = "refreshToken", timeToLive = 14440)*/
@Getter
@NoArgsConstructor
public class RefreshToken {


    private String refreshToken;

    @Id
    private String account;


    public RefreshToken(String account, String refreshToken) {
        this.account = account;
        this.refreshToken = refreshToken;
    }

    // 게터, 세터, 기타 메서드들...

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean validateRefreshToken(String refreshToken) {
        return this.refreshToken.equals(refreshToken);
    }

    public void increaseReissueCount() {
    }
}