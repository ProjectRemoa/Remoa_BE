package Remoa.BE.Web.Member.Service;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.GerneralLoginDto.*;
import Remoa.BE.Web.Member.Repository.MemberRepository;
import Remoa.BE.config.auth.RefreshToken;
import Remoa.BE.config.jwt.JwtTokenProvider;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    @Value("${security.jwt.token.refresh-expiration-minutes}")
    public long refreshExpirationMinutes;

    private final Random random = new Random();

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public Long join(Member member) {
        //  validateDuplicateMember(member);
//        member.hashPassword(this.bCryptPasswordEncoder);
        memberRepository.save(member);
        return member.getMemberId();

    }

    @Transactional
    public void deleteMemberFromDB(Member member){
        memberRepository.deleteMemberFromDB(member);
    }


    public GeneralLoginRes generalLogin(GeneralLoginReq loginReq) {
        Member member = memberRepository.findByAccount(loginReq.getAccount()).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        if (!bCryptPasswordEncoder.matches(loginReq.getPassword(), member.getPassword())) {
            throw new BaseException(CustomMessage.UNAUTHORIZED);
        }
        String token = jwtTokenProvider.createToken(member.getAccount());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getAccount());

        log.info("===============================================================");
        log.info("token : {}", token);
        log.info("refreshToken : {}", refreshToken);
        log.info("===============================================================");

        updateRefreshToken(member, refreshToken);

        return new GeneralLoginRes(token, refreshToken, member);
    }

    private void updateRefreshToken(Member member, String refreshToken) {
        // 새로운 RefreshToken 객체를 생성하고 바로 저장
        RefreshToken refreshTokenObj = new RefreshToken(member.getAccount(), refreshToken);
        redisTemplate.opsForValue().set(member.getAccount(), refreshTokenObj, refreshExpirationMinutes, TimeUnit.MINUTES);
    }

    private void validateDuplicateMember(Member member) {
        log.info("member={}", member.getEmail());
        Optional<Member> findMembers = memberRepository.findByAccount(member.getAccount());
        if (findMembers.isPresent()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    @Transactional
    public void adminSignUp(AdminSignUpReq adminSignUpReq) {
        adminSignUpReq.setPassword(bCryptPasswordEncoder.encode(adminSignUpReq.getPassword()));
        if (memberRepository.existsByAccount(adminSignUpReq.getAccount())) {
            throw new BaseException(CustomMessage.BAD_DUPLICATE);
        }
        memberRepository.save(adminSignUpReq.toEntity());
    }

    @Transactional
    public GeneralSignUpRes generalSignUp(GeneralSignUpReq signUpReq) {
        signUpReq.setPassword(bCryptPasswordEncoder.encode(signUpReq.getPassword()));
        if (memberRepository.existsByAccount(signUpReq.getAccount())) {
            throw new BaseException(CustomMessage.BAD_DUPLICATE);
        }

        String uniqueNickname = generateUniqueNickname();

        Member member = signUpReq.toEntity();
        member.setNickname(uniqueNickname);
        Member savedMember = memberRepository.save(member);

        return new GeneralSignUpRes(savedMember.getMemberId());
    }

    private String generateUniqueNickname() {
        String randomNumber;
        boolean nicknameDuplicate;
        do {
            randomNumber = Integer.toString((random.nextInt(900_000) + 100_000));
            nicknameDuplicate = memberRepository.existsByNickname("유저" + randomNumber);
        } while (nicknameDuplicate);
        return "유저" + randomNumber;
    }

    public Boolean isNicknameDuplicate(String nickname) {
        List<Member> findMembers = memberRepository.mfindByNickname(nickname);
        return !(findMembers.size() == 0);
    }


    public Member findOne(Long memberId) {
        Optional<Member> member = memberRepository.findOne(memberId);
        return member.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
    }

    public Optional<Member> findByKakaoId(Long kakaoId) {
        return memberRepository.findByKakaoId(kakaoId);
    }


}
