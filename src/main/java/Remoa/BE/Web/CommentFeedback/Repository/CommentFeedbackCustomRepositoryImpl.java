package Remoa.BE.Web.CommentFeedback.Repository;


import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Comment.Domain.QComment;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.CommentFeedback.Domain.QCommentFeedback;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.QFeedback;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Domain.QMember;
import Remoa.BE.Web.Post.Domain.Category;
import Remoa.BE.Web.Post.Domain.QPost;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentFeedbackCustomRepositoryImpl implements CommentFeedbackCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;

    QCommentFeedback commentFeedback = QCommentFeedback.commentFeedback;
    QMember member = QMember.member;
    QComment comment = QComment.comment;
    QFeedback feedback = QFeedback.feedback;
    QPost post = QPost.post;

    @Override
    public Page<CommentFeedback> findMyCommentOrFeedback(Member myMember, Pageable pageable, String sort) {

        // Subquery to find the latest commented time per post by the member
        JPAQuery<LocalDateTime> subQuery = jpaQueryFactory
                .select(commentFeedback.time.max())
                .from(commentFeedback)
                .where(commentFeedback.post.postId.eq(post.postId)
                        .and(commentFeedback.member.eq(myMember)));

        // Main query to fetch comments with latest commented time per post
        JPAQuery<CommentFeedback> query = jpaQueryFactory
                .select(commentFeedback)
                .from(commentFeedback)
                .innerJoin(commentFeedback.post, post).fetchJoin()
                .leftJoin(commentFeedback.comment, comment).fetchJoin()
                .leftJoin(commentFeedback.feedback, feedback).fetchJoin()
                .innerJoin(commentFeedback.member, member).fetchJoin()
                .where(commentFeedback.member.eq(myMember)
                        .and(commentFeedback.time.eq(subQuery)));

        // Apply sorting based on the sort parameter
        OrderSpecifier<?> orderSpecifier;
        if ("asc".equalsIgnoreCase(sort)) {
            orderSpecifier = commentFeedback.time.asc();
        } else {
            orderSpecifier = commentFeedback.time.desc();
        }
        query.orderBy(orderSpecifier);

        // Fetch total count for pagination
        long total = query.fetchCount();

        // Apply pagination to the query
        List<CommentFeedback> fetch = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Convert to Page
        return new PageImpl<>(fetch, pageable, total);
    }

    @Override
    public Optional<CommentFeedback> findByMemberOrderByTime(Member member) {
        return jpaQueryFactory.select(commentFeedback)
                .from(commentFeedback)
                .join(commentFeedback.member, this.member)
                .where(this.member.eq(member))
                .orderBy(commentFeedback.time.desc())
                .limit(1L).stream().findAny();
    }

    @Override
    public Optional<CommentFeedback> findByComment(Comment comment) {
        return jpaQueryFactory.select(commentFeedback)
                .from(commentFeedback)
                .join(commentFeedback.comment, this.comment)
                .where(this.comment.eq(comment))
                .stream().findAny();
    }

    @Override
    public Optional<CommentFeedback> findByFeedback(Feedback feedback) {
        return jpaQueryFactory.select(commentFeedback)
                .from(commentFeedback)
                .join(commentFeedback.feedback, this.feedback)
                .where(this.feedback.eq(feedback))
                .stream().findAny();
    }


    public Page<CommentFeedback> findRecentReceivedCommentFeedback(Member member, Pageable pageable, Category category) {
        boolean isCategoryExists = category != null;
        List<CommentFeedback> resultCommentFeedbacks;
        if(isCategoryExists) {
            resultCommentFeedbacks = jpaQueryFactory.select(commentFeedback)
                    .from(commentFeedback)
                    .join(commentFeedback.post, this.post)
                    .where(
                            this.post.member.eq(member), // 작성자 본인
                            this.post.category.eq(category) // 카테고리
                    )
                    .orderBy(commentFeedback.time.desc())
                    .offset(pageable.getOffset())   // 페이지 번호
                    .limit(pageable.getPageSize())  // 페이지 사이즈
                    .fetch();
        } else { // 카테고리 구분없이 전체 조회
            resultCommentFeedbacks = jpaQueryFactory.select(commentFeedback)
                    .from(commentFeedback)
                    .join(commentFeedback.post, this.post)
                    .where(this.post.member.eq(member)) // 작성자 본인
                    .orderBy(commentFeedback.time.desc())
                    .offset(pageable.getOffset())   // 페이지 번호
                    .limit(pageable.getPageSize())  // 페이지 사이즈
                    .fetch();
        }
        // 페이지네이션 기능을 위한 쿼리
        JPAQuery<CommentFeedback> countQuery = jpaQueryFactory // 총 개수
                .selectFrom(commentFeedback)
                .join(commentFeedback.post, this.post)
                .where(this.post.member.eq(member));
        // count 쿼리가 필요없는 경우는 실행하지 않는다
        return PageableExecutionUtils.getPage(resultCommentFeedbacks, pageable, () -> countQuery.fetch().size());
    }

}
