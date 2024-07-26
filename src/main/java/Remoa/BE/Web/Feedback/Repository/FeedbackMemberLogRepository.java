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

    @Query(value = "select * from feedback_member_log where post_id = :postId", nativeQuery = true)
    List<FeedbackMemberLog> findByPostHard(@Param("postId") Long postId);

    @Modifying
    @Query(value = "delete from feedback_member_log fml where post_id = :postId", nativeQuery = true)
    void deleteFeedbackLogByPostHard(@Param("postId") Long postId);

    @Modifying
    @Query(value = "delete from feedback_member_log fml where member_id = :memberId", nativeQuery = true)
    void deleteByMemberHard(@Param("memberId") Long memberId);

    boolean existsByMemberAndPost(Member member, Post post);

    Optional<FeedbackMemberLog> findByMemberAndPost(Member member, Post post);

    void deleteByMemberAndPost(Member member, Post post);
}
