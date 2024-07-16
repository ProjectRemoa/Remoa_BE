package Remoa.BE.Web.Feedback.Repository;

import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long>, FeedbackRepositoryCustom {

    @Query("select f from Feedback f where f.post = :post")
    List<Feedback> findFeedbackByPost(Post post);

    @Modifying
    @Query("delete from Feedback f where f.post = :post")
    void deleteFeedbackByPost(@Param("post") Post post);

    Page<Feedback> findByMemberOrderByFeedbackTimeDesc(Pageable pageable, Member member);

    @Modifying
    @Transactional
    @Query("DELETE FROM Feedback f WHERE f.post = :post")
    void deleteByPost(Post post);

    boolean existsByMemberAndPostAndPageNumber(Member member, Post post, Integer pageNumber);
    boolean existsByMemberAndPost(Member member, Post post);

    @Query("SELECT f FROM Feedback f " +
            "INNER JOIN FETCH f.post p " +
            "WHERE f.member = :member " +
            "AND f.feedbackTime = (SELECT MAX(f2.feedbackTime) FROM Feedback f2 WHERE f2.post.postId = f.post.postId) " +
            "ORDER BY f.feedbackTime DESC")
    Page<Feedback> findNewestFeedback(Member member, Pageable pageable);


    @Query("SELECT f FROM Feedback f " +
            "INNER JOIN FETCH f.post p " +
            "WHERE f.member = :member " +
            "AND f.feedbackTime = (SELECT MIN(f2.feedbackTime) FROM Feedback f2 WHERE f2.post.postId = f.post.postId) " +
            "ORDER BY f.feedbackTime ASC")
    Page<Feedback> findOldestFeedback(Member member, Pageable pageable);

}
