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
    @Query("delete from FeedbackReply fr where fr.member = :member")
    void deleteByMember(@Param("member") Member member);

    @Modifying
    @Query("delete from FeedbackReply fr where fr.feedbackMemberLog = :feedbackMemberLog")
    void deleteByFeedbackMemberLog(@Param("feedbackMemberLog")FeedbackMemberLog feedbackMemberLog);
}
