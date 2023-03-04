/*
package Remoa.BE.withdrew;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Repository.MemberRepository;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Member.Service.WithdrewService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations="classpath:application.yml")
@Transactional*/
/**//*

public class WithdrewTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    WithdrewService withdrewService;

    @Autowired
    EntityManager em;

    @Test
    public void 회원을_탈퇴하면_멤버테이블의_deleted_컬럼이_true로_바뀐다() throws Exception {
        //given
        Member testMember = createMember();
        memberService.join(testMember);

        em.flush();

        Long testMemberId = testMember.getMemberId();

        withdrewService.withdrewRemoa(testMember);

        em.flush();

        List<Member> allMembers = memberRepository.findAll();

        Member deletedMember = null;
        for (Member member : allMembers) {
            Long memberId = member.getMemberId();
            if (memberId == testMemberId) {
                deletedMember = member;
            }
        }
        assertEquals("탈퇴한 멤버는 조회되지 않는다.", null, deletedMember);
    }

    @Test
    public void 탈퇴한_멤버_엔티티의_deleted_필드는_true이다() throws Exception {
        //given
        Member testMember = createMember();
        memberService.join(testMember);

        em.flush();

        Long testMemberId = testMember.getMemberId();

        //when
        withdrewService.withdrewRemoa(testMember);

        em.flush();

        Optional<Member> member = memberRepository.findOne(testMemberId);
        Member findMember = member.get();


        //then
        assertEquals("회원을 탈퇴하면 해당 멤버 엔티티의 deleted 필드는 true이다.", true, findMember.getDeleted());
    }

    private Member createMember() {
        Member member = new Member();
        member.setEmail("tester@test.com");
        member.setName("test");
        member.setPassword("test");
        member.setBirth("20101010");
        member.setSex(false);
        member.setPhoneNumber("12341234");
        member.setTermConsent(true);

        return member;
    }
}
*/
