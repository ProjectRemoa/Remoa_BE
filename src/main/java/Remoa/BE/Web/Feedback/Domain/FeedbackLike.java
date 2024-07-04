package Remoa.BE.Web.Feedback.Domain;

import Remoa.BE.Web.Member.Domain.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * Member가 Feedback을 Like할 때 사용
 */
@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackLike {

    @Id
    @GeneratedValue
    @Column(name = "feedback_like_id")
    private Long feedbackLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_member_log_id")
    private FeedbackMemberLog feedbackMemberLog;

    public static FeedbackLike createFeedbackLike(Member member, FeedbackMemberLog feedbackMemberLog) {
        FeedbackLike feedbackLike = new FeedbackLike();
        feedbackLike.setFeedbackMemberLog(feedbackMemberLog);
        feedbackLike.setMember(member);

        return feedbackLike;
    }
}