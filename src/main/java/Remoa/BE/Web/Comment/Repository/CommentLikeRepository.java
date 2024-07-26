package Remoa.BE.Web.Comment.Repository;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Comment.Domain.CommentLike;
import Remoa.BE.Web.Member.Domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByMemberAndComment(Member member, Comment comment);

    @Modifying
    @Query(value = "delete from Comment_Like cl where comment_id = :commentId", nativeQuery = true)
    void deleteByCommentHard(@Param("commentId") Long commentId);
}