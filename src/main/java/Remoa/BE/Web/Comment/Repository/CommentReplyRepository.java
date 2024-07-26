package Remoa.BE.Web.Comment.Repository;

import Remoa.BE.Web.Comment.Domain.CommentReply;
import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Member.Domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentReplyRepository extends JpaRepository<CommentReply, Long> {

    List<CommentReply> findByCommentOrderByCommentRepliedTimeAsc(Comment comment);

    @Modifying
    @Query(value = "delete from CommentReply cr where comment_id = :commentId", nativeQuery = true)
    void deleteByCommentHard(@Param("commentId") Long commentId);

    @Modifying
    @Query(value = "delete from comment_reply cr where member_id = :memberId", nativeQuery = true)
    void deleteByMemberHard(@Param("memberId") Long memberId);
}
