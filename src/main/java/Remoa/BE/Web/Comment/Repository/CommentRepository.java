package Remoa.BE.Web.Comment.Repository;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    Page<Comment> findByPost(Pageable pageable, Post post);

    @Query(value = "select * from comment c where post_id =: postId", nativeQuery = true)
    List<Comment> findByPostHard(@Param("postId")Long postId);

    @Modifying
    @Query(value = "delete from Comment c where post_id = :postId", nativeQuery = true)
    void deleteByPostHard(@Param("postId") Long postId);

    Page<Comment> findByMemberOrderByCommentedTimeDesc(Pageable pageable, Member member);

    boolean existsByPost(Post post);

    @Query("SELECT c FROM Comment c " +
            "INNER JOIN FETCH c.post p " +
            "WHERE c.member = :member " +
            "AND c.commentedTime = (SELECT MAX(c2.commentedTime) FROM Comment c2 WHERE c2.post.postId = c.post.postId) " +
            "ORDER BY c.commentedTime DESC")
    Page<Comment> findNewestComment(Member member, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "INNER JOIN FETCH c.post p " +
            "WHERE c.member = :member " +
            "AND c.commentedTime = (SELECT MIN(c2.commentedTime) FROM Comment c2 " +
            "WHERE c2.post.postId = c.post.postId AND c2.member = :member) " + //
            "ORDER BY c.commentedTime ASC")
    Page<Comment> findOldestComment(Member member, Pageable pageable);


    @Modifying
    @Query(value = "delete from comment c where member_id = :memberId", nativeQuery = true)
    void deleteCommentByMemberHard(@Param("memberId") Long memberId);

}
