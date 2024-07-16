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
    @Query("delete from CommentReply cr where cr.comment = :comment")
    void deleteByComment(@Param("comment") Comment comment);

    @Modifying
    @Query("delete from CommentReply cr where cr.member = :member")
    void deleteByMember(@Param("member") Member member);
}
