package Remoa.BE.Web.Feedback.Repository;

import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackMemberLogRepository extends JpaRepository<FeedbackMemberLog, Long> {


    boolean existsByMemberAndPost(Member member, Post post);
    Optional<FeedbackMemberLog> findByMemberAndPost(Member member, Post post);
    void deleteByMemberAndPost(Member member, Post post);
}
