package Remoa.BE.Web.Member.Service;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Res.ResReIssue;
import Remoa.BE.Web.Member.Repository.MemberRepository;
import Remoa.BE.config.jwt.JwtTokenProvider;
import Remoa.BE.config.redis.RedisUtils;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RedisUtils redisUtils;


    @Transactional
    public ResReIssue reissueAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = null;
        String refreshToken = null;
        System.out.println("reissueAccessToken 진입");
        String oldAccessToken = parseBearerToken(request, HttpHeaders.AUTHORIZATION);
        if (oldAccessToken == null || oldAccessToken.isEmpty()) {
            System.out.println("만료 엑세스 토큰 없음");
            throw new BaseException(CustomMessage.EXPIRED_TOKEN_NOT_EXIST);
        }
        refreshToken = parseBearerToken(request, "refresh-token");
        if (refreshToken == null || refreshToken.isEmpty()) {
            System.out.println("리프레시 토큰 없음");
            throw new BaseException(CustomMessage.REFRESH_TOKEN_NOT_EXIST);
        }


        log.info("===============================================================");
        log.info("oldAccessToken : {}", oldAccessToken);
        log.info("refreshToken : {}", refreshToken);
        log.info("===============================================================");

        jwtTokenProvider.validateRefreshToken(refreshToken, oldAccessToken);
        newAccessToken = jwtTokenProvider.recreateAccessToken(oldAccessToken);

        log.info("===============================================================");
        log.info("new AccessToken 발급 = " + newAccessToken);
        log.info("===============================================================");
//            Authentication auth = jwtTokenProvider.getAuthentication(newAccessToken);
//            SecurityContextHolder.getContext().setAuthentication(auth);

        return new ResReIssue(newAccessToken, refreshToken);
    }

    private String parseBearerToken(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName))
                .filter(token -> token.substring(0, 7).equalsIgnoreCase("Bearer "))
                .map(token -> token.substring(7))
                .orElse(null);
    }


    public void logout(HttpServletRequest request) {
        String accessToken = jwtTokenProvider.resolveToken(request);
        String account = getAccountFromAccessToken(accessToken);
        //해당 액세스 토큰의 남은 유효 시간
        long time = jwtTokenProvider.getAccessTokenExpirationDate(accessToken).getTime() - System.currentTimeMillis();

        // AccessToken을 블랙리스트에 추가, 남은 유효시간만큼만 블랙리스트에 저장
        redisUtils.setBlackList(accessToken, account, time);
        // 리프레시 토큰도 무효화
        //   refreshTokenRepository.deleteById(account);

        // RedisUtils를 사용하여 리프레시 토큰 삭제
        redisUtils.deleteRefreshToken(account);
    }


    private String getAccountFromAccessToken(String accessToken) {
        return jwtTokenProvider.getUserAccount(accessToken);
    }

    private Member findMemberByAccount(String account) {
        return memberRepository.findByAccount(account)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
    }
}