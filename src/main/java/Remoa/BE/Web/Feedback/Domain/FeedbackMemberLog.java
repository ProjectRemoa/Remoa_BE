package Remoa.BE.Web.Feedback.Domain;


import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@SQLDelete(sql = "UPDATE feedback_member_log SET deleted = true WHERE feedback_member_log_id = ?")
@SQLRestriction("deleted = false") // 검색시 deleted = false 조건을 where 절에 추가
@NoArgsConstructor
public class FeedbackMemberLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_member_log_id")
    Long feedbackMemberLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    private Boolean deleted = Boolean.FALSE;


    @OneToMany(mappedBy = "feedbackMemberLog", cascade = {CascadeType.REMOVE}, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<FeedbackLike> feedbackLikes;

    @OneToMany(mappedBy = "feedbackMemberLog", cascade = {CascadeType.REMOVE}, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<FeedbackReply> feedbackReplies;

    public void increaseLikeCount(){
        this.likeCount++;
    }

    public void decreaseLikeCount(){
        this.likeCount--;
    }

    public FeedbackMemberLog(Member member, Post post) {
        this.member = member;
        this.post = post;
    }
}
