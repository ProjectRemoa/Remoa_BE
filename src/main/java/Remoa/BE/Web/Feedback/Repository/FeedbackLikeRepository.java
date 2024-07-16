package Remoa.BE.Web.Feedback.Repository;

import Remoa.BE.Web.Feedback.Domain.FeedbackLike;
import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Member.Domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackLikeRepository extends JpaRepository<FeedbackLike, Long> {
    Optional<FeedbackLike> findByMemberAndFeedbackMemberLog(Member member, FeedbackMemberLog feedbackMemberLog);

    @Modifying
    @Query("delete from FeedbackLike fl where fl.feedbackMemberLog = :feedbackMemberLog")
    void deleteByFeedback(@Param("feedbackMemberLog") FeedbackMemberLog feedbackMemberLog);
}