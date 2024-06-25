package Remoa.BE.config;

import Remoa.BE.Web.Member.Domain.Role;
import Remoa.BE.config.jwt.CustomAuthenticationEntryPoint;
import Remoa.BE.config.jwt.JwtAccessDeniedHandler;
import Remoa.BE.config.jwt.JwtAuthenticationFilter;
import Remoa.BE.config.jwt.JwtTokenProvider;
import Remoa.BE.config.redis.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import static Remoa.BE.config.auth.AuthConstant.*;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecureConfig {

    public static final String FRONT_URL = "http://localhost:3000";

    private final CorsFilter corsFilter;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisUtils redisUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(CsrfConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilter(corsFilter);

        http.authorizeHttpRequests(request -> request
                        //  .requestMatchers(AUTH_BLACKLIST).authenticated()
                        .requestMatchers(HttpMethod.GET, GET_AUTH_BLACKLIST).authenticated()
                        .requestMatchers(HttpMethod.POST, POST_AUTH_BLACKLIST).authenticated()
                        .requestMatchers(HttpMethod.PUT, PUT_AUTH_BLACKLIST).authenticated()
                        .requestMatchers(HttpMethod.DELETE, DELETE_AUTH_BLACKLIST).authenticated()
                        .requestMatchers(HttpMethod.POST, ADMIN_POST_AUTH_BLACKLIST).hasAuthority(Role.ADMIN.toString()) // ADMIN 권한
                        .requestMatchers(HttpMethod.PUT, ADMIN_PUT_AUTH_BLACKLIST).hasAuthority(Role.ADMIN.toString()) // ADMIN 권한
                        .requestMatchers(HttpMethod.DELETE, ADMIN_DELETE_AUTH_BLACKLIST).hasAuthority(Role.ADMIN.toString()) // ADMIN 권한
                        //인증되어야 들어갈 수 있다.
                        .anyRequest().permitAll())
                // 나머지는 모두 허용
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(new JwtAccessDeniedHandler())); //권한 403 관련
        /**
         AuthenticationEntryPoint
         인증이 되지않은 유저가 요청을 했을때 동작함
         */

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider,redisUtils), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}