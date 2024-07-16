package Remoa.BE.Web.Feedback.Repository;

import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackMemberLogRepository extends JpaRepository<FeedbackMemberLog, Long> {

    List<FeedbackMemberLog> findByPost(Post post);

    @Modifying
    @Query("delete from FeedbackMemberLog fml where fml.post = :post")
    void deleteFeedbackLogByPost(@Param("post") Post post);

    @Modifying
    @Query("delete from FeedbackMemberLog fml where fml.member = :member")
    void deleteByMember(@Param("member") Member member);

    boolean existsByMemberAndPost(Member member, Post post);

    Optional<FeedbackMemberLog> findByMemberAndPost(Member member, Post post);

    void deleteByMemberAndPost(Member member, Post post);
}
