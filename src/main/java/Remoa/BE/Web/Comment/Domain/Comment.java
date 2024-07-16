package Remoa.BE.Web.Comment.Domain;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE comment SET deleted = true WHERE comment_id = ?")
@SQLRestriction("deleted = false") // 검색시 deleted = false 조건을 where 절에 추가
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    /**
     * Comment가 속해있는 Post
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * Comment를 작성한 Member
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * Comment의 내용
     */
    private String content;


    /**
     * Comment가 작성된 시간
     */
    @Column(name = "commented_time")
    private LocalDateTime commentedTime;

    /**
     * Comment의 좋아요 숫자
     */
    @Column(name = "like_count")
    private Integer LikeCount = 0;


    @OneToMany(mappedBy = "comment", cascade = {CascadeType.REMOVE}, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<CommentLike> commentLikes;


    @OneToMany(mappedBy = "comment", cascade = {CascadeType.REMOVE}, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<CommentReply> commentReplies;

    private Boolean deleted = Boolean.FALSE;


    public static Comment createComment(Post post, Member member, String content, LocalDateTime time) {
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setMember(member);
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setCommentedTime(time);
        return comment;
    }
}