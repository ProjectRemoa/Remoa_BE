package Remoa.BE.Web.Comment.Repository;

import Remoa.BE.Web.Comment.Domain.CommentLike;
import Remoa.BE.Web.Comment.Domain.QComment;
import Remoa.BE.Web.Member.Domain.QMember;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Member.Domain.CommentBookmark;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.QPost;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final EntityManager em;

    public Optional<Comment> findOne(Long id) {
        return Optional.ofNullable(em.find(Comment.class, id));
    }

    public void saveComment(Comment comment) {
        em.persist(comment);
    }

    public Optional<Comment> findByCommentId(Long commentId) {
        return Optional.ofNullable(em.find(Comment.class, commentId));
    }

    /**
     * 포스트 별 코멘트을 찾아오기 위한 메서드
     *
     * @param post
     * @return List<Comment>
     */
    public List<Comment> findByPost(Post post) {
        return em.createQuery("select c from Comment c where c.post = :post order by c.commentedTime asc",
                        Comment.class)
                .setParameter("post", post)
                .getResultList();
    }

    public void saveCommentLike(CommentLike commentLike) {
        em.persist(commentLike);
    }

    /**
     * commentLikeAction 메서드를 실행하기 전 이미 해당 코멘트에 대한 좋아요를 했는지 검증->service 단에서 return 값의 null이면 좋아요 가능.
     * 혹은 좋아요 취소를 위해 사용할 수도 있다.
     */
    public Optional<CommentLike> findMemberCommendLike(Member member, Comment comment) {
        return em.createQuery("select cl from CommentLike cl " +
                        "where cl.comment = :comment and cl.member = :member", CommentLike.class)
                .setParameter("comment", comment)
                .setParameter("member", member)
                .getResultStream()
                .findAny();
    }

    public Integer findCommentLike(Comment comment) {
        return em.createQuery("select cl from CommentLike cl where cl.comment = :comment", CommentLike.class)
                .setParameter("comment", comment)
                .getResultList()
                .size();
    }

    public void saveCommentBookmark(CommentBookmark commentBookmark) {
        em.persist(commentBookmark);
    }

    /**
     * commentBookmarkAction 메서드를 실행하기 전 이미 해당 코멘트에 대한 북마크를 했는지 검증->service 단에서 return 값의 null이면 북마크 가능.
     * 혹은 북마크 해제를 위해 사용할 수도 있다.
     */
    public Optional<CommentBookmark> findMemberCommendBookmark(Member member, Comment comment) {
        return em.createQuery("select cb from CommentBookmark cb " +
                        "where cb.comment = :comment and cb.member = :member", CommentBookmark.class)
                .setParameter("comment", comment)
                .setParameter("member", member)
                .getResultStream()
                .findAny();
    }

    /* //필요없을 거 같아서 주석처리...
    public Integer findCommentBookmark(Comment comment) {
        return em.createQuery("select cb from CommentBookmark cb where cb.comment = :comment", CommentBookmark.class)
                .setParameter("comment", comment)
                .getResultList()
                .size();
    }*/

    public List<Comment> findRepliesOfParentComment(Comment parentComment) {
        return em.createQuery("select c from Comment c " +
                        "where c.parentComment = :comment order by c.commentedTime desc", Comment.class)
                .setParameter("comment", parentComment)
                .getResultList();
    }

    public void updateComment(Comment newComment) {
        em.merge(newComment);
    }

    public void deleteComment(Comment comment) {
        em.remove(comment);
    }

    public List<Comment> findAllByMember(Member member) {
        return em.createQuery("select c from Comment c " +
                        "where c.member = :member", Comment.class)
                .setParameter("member", member)
                .getResultList();
    }


    public void deleteChildCommentByParentFeedback(Comment comment) {
        em.createQuery("delete from Comment c where c.parentComment = :comment")
                .setParameter("comment", comment)
                .executeUpdate();
    }

    private final JPAQueryFactory jpaQueryFactory;
    QComment comment = QComment.comment;
    QMember member = QMember.member;
    QPost post = QPost.post;


    @Override
    public Page<Comment> findMyComment(Member myMember, Pageable pageable, String sort) {


        // Subquery to find the latest commented time per post by the member
        JPAQuery<LocalDateTime> subQuery = jpaQueryFactory
                .select(comment.commentedTime.max())
                .from(comment)
                .where(comment.post.postId.eq(post.postId)
                        .and(comment.member.eq(myMember)));

        // Main query to fetch comments with latest commented time per post
        JPAQuery<Comment> query = jpaQueryFactory
                .select(comment)
                .from(comment)
                .innerJoin(comment.post, post).fetchJoin()
                .innerJoin(comment.member, member).fetchJoin()
                .where(comment.member.eq(myMember)
                        .and(comment.commentedTime.eq(subQuery)));

        // Apply sorting based on the sort parameter
        OrderSpecifier<?> orderSpecifier;
        if ("asc".equalsIgnoreCase(sort)) {
            orderSpecifier = comment.commentedTime.asc();
        } else {
            orderSpecifier = comment.commentedTime.desc();
        }
        query.orderBy(orderSpecifier);

        // Fetch total count for pagination
        long total = query.fetchCount();

        // Apply pagination to the query
        List<Comment> fetch = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Convert to Page
        return new PageImpl<>(fetch, pageable, total);
    }
}
