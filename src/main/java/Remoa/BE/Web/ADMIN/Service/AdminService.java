package Remoa.BE.Web.ADMIN.Service;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Comment.Domain.CommentReply;
import Remoa.BE.Web.Comment.Repository.CommentReplyRepository;
import Remoa.BE.Web.Comment.Repository.CommentRepository;
import Remoa.BE.Web.CommentFeedback.Repository.CommentFeedbackRepository;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackReply;
import Remoa.BE.Web.Feedback.Repository.FeedbackReplyRepository;
import Remoa.BE.Web.Feedback.Repository.FeedbackRepository;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Repository.PostRepository;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentReplyRepository commentReplyRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackReplyRepository feedBackReplyRepository;
    private final CommentFeedbackRepository commentFeedbackRepository;

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        postRepository.delete(post);
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        commentFeedbackRepository.deleteByComment(comment);
        commentRepository.delete(comment);
    }

    public void deleteCommentReply(Long commentReplyId) {
        CommentReply commentReply = commentReplyRepository.findById(commentReplyId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        commentReplyRepository.delete(commentReply);
    }

    public void deleteFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        commentFeedbackRepository.deleteByFeedback(feedback);
        feedbackRepository.delete(feedback);
    }

    public void deleteFeedbackReply(Long feedbackReplyId) {
        FeedbackReply feedbackReply = feedBackReplyRepository.findById(feedbackReplyId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        feedBackReplyRepository.delete(feedbackReply);
    }
}

