package Remoa.BE.Web.Post.Domain;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Member.Domain.Member;
import jakarta.persistence.CascadeType;
import lombok.*;
import org.hibernate.annotations.*;

import jakarta.persistence.*;
import org.hibernate.annotations.processing.SQL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE post SET deleted = true WHERE post_id = ?") // 사용자 정의 SQL DELETE 문 설정 sofe delete로 구현
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    /**
     * 해당 Post를 쓴 작성자(Member)
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * Post 제목
     */
    private String title;

    @OneToOne
    private UploadFile thumbnail;

    /**
     * 참여 공모전의 이름
     */
    @Column(name = "contest_name")
    private String contestName;

    /**
     * 유튜브 링크
     */
    @Builder.Default
    private String youtubeLink = "";

    /**
     * 참여한 공모전의 마감 기한
     */
    private String deadline;


    /**
     * pm쪽에 문의해야할듯.
     */
    @Column(name = "contest_aware_type")
    private String contestAwardType;

    /**
     * Post에 대한 좋아요 수
     */
    @Builder.Default
    @Column(name = "like_count")
    private Integer likeCount = 0;

    /**
     * Post가 작성된 시간
     */
    @Column(name = "posting_time")
    private LocalDateTime postingTime;

    /**
     * Post의 조회수
     */
    @Builder.Default
    private Integer views = 0;

    @Builder.Default
    private Integer scrapCount = 0;

   /* @Builder.Default
    private Integer commentCount = 0;

    @Builder.Default
    private Integer feedbackCount = 0;*/

    @Builder.Default
    private Integer pageCount = 1;

    /**
     * Post에 작성되어진 Comment
     */
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<Feedback> feedbacks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<CommentFeedback> commentFeedbacks = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<PostScrap> postScraps = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = LAZY)
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private List<PostLike> postLikes = new ArrayList<>();
    /**
     * Post에서 쓰인 files
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = LAZY)
    private List<UploadFile> uploadFiles;

    /**
     * 작성한 Post의 카테고리
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder.Default
    private Boolean deleted = Boolean.FALSE;


    public void addViewCount() {
        this.views++;
    }
}
