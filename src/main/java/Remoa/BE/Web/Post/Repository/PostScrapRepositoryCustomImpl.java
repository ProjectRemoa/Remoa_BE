package Remoa.BE.Web.Post.Repository;

import Remoa.BE.Web.Comment.Domain.QComment;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.CommentFeedback.Domain.QCommentFeedback;
import Remoa.BE.Web.Feedback.Domain.QFeedback;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Domain.QMember;
import Remoa.BE.Web.Post.Domain.Category;
import Remoa.BE.Web.Post.Domain.PostScrap;
import Remoa.BE.Web.Post.Domain.QPost;
import Remoa.BE.Web.Post.Domain.QPostScrap;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostScrapRepositoryCustomImpl implements PostScrapRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    QCommentFeedback commentFeedback = QCommentFeedback.commentFeedback;
    QMember member = QMember.member;
    QPost post = QPost.post;
    QPostScrap postScrap = QPostScrap.postScrap;

    @Override
    public Page<PostScrap> findMyScrapedPost(Member myMember, Pageable pageable, Category category, String sort) {
        boolean isCategoryExists = category != null;

        JPAQuery<PostScrap> query = jpaQueryFactory.select(postScrap)
                .from(postScrap)
                .innerJoin(postScrap.post, post).fetchJoin()
                .innerJoin(postScrap.post.member, member).fetchJoin()
                .where(
                        postScrap.member.eq(myMember)
                                .and(isCategoryExists ? postScrap.post.category.eq(category) : null)
                );
        // Apply sorting based on the sort parameter
        OrderSpecifier<?> orderSpecifier;
        if ("asc".equalsIgnoreCase(sort)) {
            orderSpecifier = postScrap.scrapTime.asc();
        } else {
            orderSpecifier = postScrap.scrapTime.desc();
        }

        List<PostScrap> result = query.orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이지네이션 기능을 위한 카운트 쿼리
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(postScrap.count())
                .from(postScrap)
                .innerJoin(postScrap.post, post)
                .where(
                        postScrap.member.eq(myMember)
                                .and(isCategoryExists ? postScrap.post.category.eq(category) : null)
                );

        // 페이지네이션 적용
        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

}
