package Remoa.BE.Web.CommentFeedback.Repository;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommentFeedbackCustomRepository  {
    Optional<CommentFeedback> findByMemberOrderByTime(Member member);
    Optional<CommentFeedback> findByComment(Comment comment);
    Optional<CommentFeedback> findByFeedback(Feedback feedback);

    Page<CommentFeedback> findRecentReceivedCommentFeedback(Member member, Pageable pageable, Category category);
    Page<CommentFeedback> findMyCommentOrFeedback(Member member, Pageable pageable, String sort);
}
