package Remoa.BE.Web.Feedback.Domain;

import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE feedback_reply SET deleted = true WHERE feedback_reply_id = ?")
@SQLRestriction("deleted = false") // 검색시 deleted = false 조건을 where 절에 추가
public class FeedbackReply {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_reply_id")
    private Long feedbackReplyId;

    /**
     * FeedbackReply 속해있는 Post
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * FeedbackReply 작성한 Member
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "feedback_member_log_id")
    private FeedbackMemberLog feedbackMemberLog;

    /**
     * FeedbackReply 내용
     */
    private String content;

    /**
     * FeedbackReply 좋아요 숫자
     */
    @Column(name = "like_count")
    private Integer likeCount = 0;

    /**
     * FeedbackReply 작성된 시간
     */
    @Column(name = "feedback_reply_time")
    private LocalDateTime feedbackReplyTime;


    @OneToMany(mappedBy = "feedbackReply", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<FeedbackReplyLike> feedbackReplyLikes;

    private Boolean deleted = Boolean.FALSE;

    public static FeedbackReply createFeedbackReply(Post post, Member member, FeedbackMemberLog feedbackMemberLog, String content) {
        FeedbackReply feedbackReply = new FeedbackReply();
        feedbackReply.setPost(post);
        feedbackReply.setMember(member);
        feedbackReply.setFeedbackMemberLog(feedbackMemberLog);
        feedbackReply.setContent(content);
        feedbackReply.setLikeCount(0);
        feedbackReply.setFeedbackReplyTime(LocalDateTime.now());
        return feedbackReply;
    }
}
