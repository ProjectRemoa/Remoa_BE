package Remoa.BE.Web.Comment.Repository;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Service.MemberService;
import jdk.jfr.StackTrace;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class CommentRepositoryCustomImplTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    MemberService memberService;

    @Test
    void sqlTest() {
        Member one = memberService.findOne(1L);
     //   commentRepository.findMyComment(one)
    }
}