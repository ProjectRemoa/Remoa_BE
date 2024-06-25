package Remoa.BE.config.jwt;

import Remoa.BE.config.auth.MemberDetailsService;
import Remoa.BE.config.auth.RefreshToken;
import Remoa.BE.config.redis.RedisUtils;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
@Slf4j
public class JwtTokenProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private Key key;
    private final long expirationMinutes;
    private final long refreshExpirationMinutes;
    private final MemberDetailsService memberDetailsService;
    private final RedisUtils redisUtils;

    public JwtTokenProvider(@Value("${security.jwt.token.secret-key}") String secretKey,
                            @Value("${security.jwt.token.expiration-minutes}") long expirationMinutes,    // hours -> minutes
                            @Value("${security.jwt.token.refresh-expiration-minutes}") long refreshExpirationMinutes,    // 추가
                            MemberDetailsService memberDetailsService,
                            RedisUtils redisUtils) {

        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMinutes = expirationMinutes;
        this.refreshExpirationMinutes = refreshExpirationMinutes;
        this.memberDetailsService = memberDetailsService;
        this.redisUtils = redisUtils;
    }

    @Transactional(readOnly = true)
    public void validateRefreshToken(String refreshToken, String oldAccessToken) throws JsonProcessingException {
        try {
            log.debug("Validating refresh token: {}", refreshToken);
            validateToken(refreshToken);
            log.debug("Extracting account from old access token: {}", oldAccessToken);
            String account = getUserAccountFromOldToken(oldAccessToken);
            log.debug("Finding refresh token for account: {}", account);
            Optional<RefreshToken> byAccount = Optional.ofNullable((RefreshToken) redisUtils.get(account));

            log.debug("Validating refresh token for account: {}", account);
            Optional<RefreshToken> refreshToken1 = byAccount.filter(memberRefreshToken -> memberRefreshToken.validateRefreshToken(refreshToken));

            log.debug("Checking if refresh token is valid for account: {}", account);
            refreshToken1.orElseThrow(() -> new ExpiredJwtException(null, null, "Refresh token expired."));
        } catch (BaseException e) {
            log.error("BaseException: {}", e.getMessage(), e);
            throw e;
        } catch (ExpiredJwtException e) {
            log.error("ExpiredJwtException: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected Exception: {}", e.getMessage(), e);
            throw new BaseException(CustomMessage.SERVER_ERROR);
        }
    }

    public String getUserAccountFromOldToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        }
    }

    @Transactional
    public String recreateAccessToken(String oldAccessToken) throws JsonProcessingException {
        if (oldAccessToken == null) {
            throw new BaseException(CustomMessage.BAD_REQUEST);
        }
        String account = getUserAccountFromOldToken(oldAccessToken);
        return createToken(account);
    }

    public String createToken(String account) { //email 받음
        Claims claims = Jwts.claims().setSubject(account); // JWT payload에 저장되는 정보 단위
        claims.put("account", account); // key/ value 쌍으로 저장

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationMinutes * 60000); // set Expire Time
//        log.info("now: {}", now);
//        log.info("validity: {}", validity);

        return Jwts.builder()
                .setClaims(claims)  // sub 설정 (정보 저장)
                .setIssuedAt(now)   // 토큰 발행 시간 정보
                .setExpiration(validity) // Set Expire Time
                .signWith(key, SignatureAlgorithm.HS256) //서명하는 값은 우리가 임의로 설정 -> YAML파일
                // 사용할 암호화 알고리즘과 signature에 들어갈 secret값 세팅
                .compact();
    }

    public String createRefreshToken(String account) { //email 받음
        Claims claims = Jwts.claims().setSubject(account); // JWT payload에 저장되는 정보 단위
        claims.put("account", account); // key/ value 쌍으로 저장

        Date now = new Date();
        // 리프레시 토큰의 만료 시간 설정 (액세스 토큰의 만료 시간보다 더 길게 설정)
        Date validity = new Date(now.getTime() + refreshExpirationMinutes * 60000); // hours -> milliseconds

        return Jwts.builder()
                .setClaims(claims)  // sub 설정 (정보 저장)
                .setIssuedAt(now)   // 토큰 발행 시간 정보
                .setExpiration(validity) // Set Expire Time
                .signWith(key, SignatureAlgorithm.HS256) //서명하는 값은 우리가 임의로 설정 -> YAML파일
                // 사용할 암호화 알고리즘과 signature에 들어갈 secret값 세팅
                .compact();
    }

    public Long getSubject(String token) {
        return Long.valueOf(Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }

    // Jwt Token에서 account 추출
    public String getUserAccount(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /*
    인증 성공시 SecurityContextHolder에 저장할 Authentication 객체 생성
    jwt토큰으로부터 이걸 디코딩 할 경우 account를 얻을 수 있다. account를 얻고
    MemberDetailsService에서 account를 통해 MemberDetails객체를 생성.
    */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = memberDetailsService.loadUserByUsername(this.getUserAccount(token));
        log.info("Jwt 토큰으로부터 account 얻어 냄"); // 토큰으로부터 MemberDetails를 생성하고 이를 토대로 인증 객체를 리턴
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); //헤더 없을 경우 null
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtProperties.TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        } //토큰만 파싱해서 리턴
        return null;
    }

    // Token의 유효성 + 만료 기간 검사
    public void validateToken(String jwtToken) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(jwtToken);
    }

    public Date getAccessTokenExpirationDate(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

}
