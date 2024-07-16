package Remoa.BE.Web.Feedback.Domain;

import Remoa.BE.Web.Comment.Domain.CommentReply;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import jakarta.persistence.CascadeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE feedback SET deleted = true WHERE feedback_id = ?")
@SQLRestriction("deleted = false") // 검색시 deleted = false 조건을 where 절에 추가
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    /**
     * Feedback이 속해있는 Post
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * Feedback을 작성한 Member
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "page_number")
    private Integer pageNumber;

    /**
     * Feedback의 내용
     */
    private String content;

    /**
     * Feedback이 작성된 시간
     */
    @Column(name = "feedback_time")
    private LocalDateTime feedbackTime;

    /**
     * Feedback의 좋아요 숫자
     */
    @Column(name = "like_count")
    private Integer likeCount = 0;






    private Boolean deleted = Boolean.FALSE;


    public static Feedback createFeedback(Post post, Member member, Integer pageNumber, String content, LocalDateTime time) {
        Feedback feedbackObj = new Feedback();
        feedbackObj.setPost(post);
        feedbackObj.setMember(member);
        feedbackObj.setPageNumber(pageNumber);
        feedbackObj.setContent(content);
        feedbackObj.setLikeCount(0);
        feedbackObj.setFeedbackTime(time);
        return feedbackObj;
    }
}