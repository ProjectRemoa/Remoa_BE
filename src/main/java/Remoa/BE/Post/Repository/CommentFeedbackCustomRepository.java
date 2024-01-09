package Remoa.BE.Post.Repository;

import Remoa.BE.Member.Domain.Comment;
import Remoa.BE.Member.Domain.CommentFeedback;
import Remoa.BE.Member.Domain.Feedback;
import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Post.Domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentFeedbackCustomRepository {
    Optional<CommentFeedback> findByMemberOrderByTime(Member member);
    Optional<CommentFeedback> findByComment(Comment comment);
    Optional<CommentFeedback> findByFeedback(Feedback feedback);
    void deleteByMember(Member member);
    public Page<CommentFeedback> findRecentReceivedCommentFeedback(Member member, Pageable pageable, Category category);
}
