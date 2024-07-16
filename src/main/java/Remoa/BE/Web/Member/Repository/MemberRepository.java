package Remoa.BE.Web.Member.Repository;

import Remoa.BE.Web.Member.Domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);


    boolean existsByAccount(String account);

    Optional<Member> findByAccount(String account);

    @Modifying
    @Transactional
    @Query("DELETE FROM Member m WHERE m = :member")
    void deleteMemberFromDB(@Param("member") Member member);

    @Query("select m from Member m where m.memberId = :id")
    Optional<Member> findOne(@Param("id") Long id);

    @Query("select m from Member m")
    List<Member> findAll();

    @Query("select m from Member m where m.email = :email")
    Optional<Member> findByEmail(@Param("email") String email);

    @Query("select m from Member m where m.nickname = :nickname")
    List<Member> mfindByNickname(@Param("nickname") String nickname);

    Optional<Member> findByNickname(String nickName);

    @Query("select m from Member m where m.kakaoId = :kakaoId")
    Optional<Member> findByKakaoId(@Param("kakaoId") Long kakaoId);

    // 나머지 메소드들도 유사하게 @Query를 이용하여 변경 가능


}
