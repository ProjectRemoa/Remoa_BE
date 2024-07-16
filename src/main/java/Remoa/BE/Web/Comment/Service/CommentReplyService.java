package Remoa.BE.Web.Comment.Service;

import Remoa.BE.Web.Comment.Domain.CommentReply;
import Remoa.BE.Web.Comment.Domain.CommentReplyLike;
import Remoa.BE.Web.Comment.Repository.CommentReplyLikeRepository;
import Remoa.BE.Web.Comment.Repository.CommentReplyRepository;
import Remoa.BE.Web.Comment.Repository.CommentRepository;
import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Repository.PostRepository;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentReplyService {

    private final CommentReplyRepository commentReplyRepository;
    private final CommentReplyLikeRepository commentReplyLikeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public List<CommentReply> findCommentReplies(Comment comment) {
        List<CommentReply> commentReplies = commentReplyRepository.findByCommentOrderByCommentRepliedTimeAsc(comment);
        return commentReplies;
    }

    @Transactional
    public void deleteByMember(Member member){
        commentReplyRepository.deleteByMember(member);
    }

    @Transactional
    public void deleteByComment(Comment comment){
        commentReplyRepository.deleteByComment(comment);
    }


    @Transactional
    public CommentReply registerCommentReply(Member member, String content, Long postId, Long commentId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));;
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));;

        CommentReply commentReply = CommentReply.createCommentReply(post, member, content, comment);
        commentReplyRepository.save(commentReply);
        return commentReply;
    }

    public CommentReply findOne(Long replyId) {
        Optional<CommentReply> reply = commentReplyRepository.findById(replyId);
        return reply.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment reply not found"));
    }

    @Transactional
    public void modifyCommentReply(String commentReplyContent, Long commentReplyId) {
        CommentReply commentReply = commentReplyRepository.findById(commentReplyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment reply not found"));
        commentReply.setContent(commentReplyContent); // 변경 감지
    }

    @Transactional
    public void deleteCommentReply(Long commentReplyId) {
        CommentReply commentReply = commentReplyRepository.findById(commentReplyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment reply not found"));
        commentReplyRepository.delete(commentReply);
    }

    public Optional<CommentReplyLike> findCommentReplyLike(Member member, CommentReply commentReply) {
        return commentReplyLikeRepository.findByMemberAndCommentReply(member, commentReply);
    }

    @Transactional
    public void likeCommentReply(Member member, Long commentReplyId) {
        CommentReply commentReplyObj = findOne(commentReplyId);
        Integer commentReplyLikeCount = commentReplyObj.getLikeCount();

        // CommentReplyLike를 db에서 조회해보고 조회 결과가 null이면 like+=1, CommentReplyLike 엔티티 생성
        // null이 아니면 like -= 1, 조회결과인 해당 CommentReplyLike 엔티티 삭제
        Optional<CommentReplyLike> commentReplyLike = findCommentReplyLike(member, commentReplyObj);
        if (commentReplyLike.isEmpty()) {
            commentReplyObj.setLikeCount(commentReplyLikeCount + 1); // 좋아요 수 1 증가
            CommentReplyLike commentReplyLikeObj = CommentReplyLike.createCommentReplyLike(member, commentReplyObj);
            commentReplyLikeRepository.save(commentReplyLikeObj);
        } else {
            commentReplyObj.setLikeCount(commentReplyLikeCount - 1); // 좋아요 수 1 차감
            commentReplyLikeRepository.deleteById(commentReplyLike.get().getCommentReplyLikeId()); // db에서 삭제
        }
    }

    public int commentReplyLikeCount(Long commentReplyId) {
        CommentReply commentReply = findOne(commentReplyId);
        return commentReply.getLikeCount();
    }
}
