package Remoa.BE.Web.Feedback.Repository;

import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Feedback.Domain.FeedbackReply;
import Remoa.BE.Web.Member.Domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackReplyRepository extends JpaRepository<FeedbackReply, Long> {

    List<FeedbackReply> findByFeedbackMemberLogOrderByFeedbackReplyTimeAsc(FeedbackMemberLog feedbackMemberLog);


    @Modifying
    @Query(value = "delete from feedback_reply fr where fr.member_id = :memberId", nativeQuery = true)
    void deleteByMemberHard(@Param("memberId") Long memberId);

    @Modifying
    @Query(value = "delete from feedback_reply fr where fr.feedback_member_log_id = :feedbackMemberLogId", nativeQuery = true)
    void deleteByFeedbackMemberLogHard(@Param("feedbackMemberLogId")Long feedbackMemberLogId);
}
