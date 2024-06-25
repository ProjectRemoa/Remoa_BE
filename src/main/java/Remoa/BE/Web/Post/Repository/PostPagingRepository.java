package Remoa.BE.Web.Post.Repository;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Category;
import Remoa.BE.Web.Post.Domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Query Creation을 사용해서 post data sorting & slicing
 */
@Repository
public interface PostPagingRepository extends PagingAndSortingRepository<Post, Long>, PostCustomRepository {

    @Query("SELECT p FROM Post p WHERE (p.member.nickname LIKE %:searchQuery% OR p.title LIKE %:searchQuery%) AND p.category = :category")
    Page<Post> findByMemberNameOrTitleContainingAndCategory(Pageable pageable, String searchQuery, Category category);

    Page<Post> findByMemberAndTitleContainingOrderByPostingTimeDesc(Pageable pageable, Member member, String title);

    Page<Post> findByMemberAndTitleContainingOrderByViewsDesc(Pageable pageable, Member member, String title);

    Page<Post> findByMemberAndTitleContainingOrderByLikeCountDesc(Pageable pageable, Member member, String title);

    Page<Post> findByMemberAndTitleContainingOrderByScrapCountDesc(Pageable pageable, Member member, String title);

    Page<Post> findByMemberAndCategoryAndTitleContaining(Pageable pageable, Member member, Category category, String title);

    //"IsNotEmpty" 부분에 "Cannot resolve property 'isNotEmpty'"경고가 나오는 건 JPA의 isNotEmpty 예약어를 intellij가 인식하지 못하고 자바의 프로퍼티로 인식하기 때문. 즉, 무시해도 됨.
   // Page<Post> findByMemberAndCategoryAndCommentsIsNotEmpty(Pageable pageable, Member member, Category category);

    //"IsNotEmpty" 부분에 "Cannot resolve property 'isNotEmpty'"경고가 나오는 건 JPA의 isNotEmpty 예약어를 intellij가 인식하지 못하고 자바의 프로퍼티로 인식하기 때문. 즉, 무시해도 됨.
   // Page<Post> findByMemberAndCommentsIsNotEmpty(Pageable pageable, Member member);

    Page<Post> findByTitleContaining(Pageable pageable, String title);

    @Query("SELECT p FROM Post p WHERE p.member.nickname LIKE %:searchQuery% OR p.title LIKE %:searchQuery%")
    Page<Post> findByMemberNameOrTitleContaining(Pageable pageable, String searchQuery);

    List<Post> findByMemberRecentTwelve(Member member);

}
