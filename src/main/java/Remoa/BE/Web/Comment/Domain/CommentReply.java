package Remoa.BE.Web.Comment.Domain;

import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
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
@SQLDelete(sql = "UPDATE comment_reply SET deleted = true WHERE comment_reply_id = ?")
@SQLRestriction("deleted = false") // 검색시 deleted = false 조건을 where 절에 추가
public class CommentReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_reply_id")
    private Long commentReplyId;

    /**
     * CommentReply 속해있는 Post
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * CommentReply 작성한 Member
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * 대댓글 기능을 위해 부모 댓글과의 연관관계 세팅.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;


    /**
     * CommentReply 내용
     */
    @Lob
    @Column(name = "content", length = 300)
    private String content;

    /**
     * CommentReply 작성된 시간
     */
    @Column(name = "comment_replied_time")
    private LocalDateTime commentRepliedTime;

    /**
     * CommentReply 좋아요 숫자
     */
    @Column(name = "like_count")
    private Integer likeCount = 0;


    @OneToMany(mappedBy = "commentReply", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<CommentReplyLike> commentReplyLikes;

    private Boolean deleted = Boolean.FALSE;

    public static CommentReply createCommentReply(Post post, Member member, String content, Comment parentComment) {
        if(content.length() > 300){
            throw new BaseException(CustomMessage.INVALID_CONTENT_LENGTH);
        }
        CommentReply commentReply = new CommentReply();
        commentReply.setPost(post);
        commentReply.setMember(member);
        commentReply.setContent(content);
        commentReply.setComment(parentComment);
        commentReply.setLikeCount(0);
        commentReply.setCommentRepliedTime(LocalDateTime.now());
        return commentReply;
    }

    public void setContent(String content){
        if(content.length() > 300){
            throw new BaseException(CustomMessage.INVALID_CONTENT_LENGTH);
        }
        this.content = content;
    }

}
