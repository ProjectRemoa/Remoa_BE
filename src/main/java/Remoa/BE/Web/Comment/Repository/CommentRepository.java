package Remoa.BE.Web.Comment.Repository;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    Page<Comment> findByPost(Pageable pageable, Post post);

    Page<Comment> findByMemberOrderByCommentedTimeDesc(Pageable pageable, Member member);

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

}
