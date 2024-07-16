package Remoa.BE.Web.Comment.Service;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Comment.Domain.CommentLike;
import Remoa.BE.Web.Comment.Repository.CommentRepository;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Comment.Repository.CommentLikeRepository;
import Remoa.BE.Web.CommentFeedback.Service.CommentFeedbackService;
import Remoa.BE.Web.Post.Repository.PostRepository;
import Remoa.BE.Web.Member.Domain.*;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static Remoa.BE.utill.Constant.CONTENT_PAGE_SIZE;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final CommentFeedbackService commentFeedbackService;
    private final MemberService memberService;

    @Transactional
    public void deleteByMember(Member member){
        commentRepository.deleteCommentByMember(member);
    }

    @Transactional
    public Long writeComment(Comment comment) {
        commentRepository.saveComment(comment);
        return comment.getCommentId();
    }

    @Transactional
    public void deleteCommentLikeByComment(Comment comment){
        commentLikeRepository.deleteByComment(comment);
    }

    @Transactional
    public void deleteByPost(Post post){
        commentRepository.deleteByPost(post);
    }

    public List<Comment> findAllCommentsOfPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        return commentRepository.findByPost(post);
    }

    public List<Comment> findCommentsByPost(Post post) {
        return commentRepository.findByPost(post);
    }

    @Transactional
    public Long commentLikeAction(Comment comment, Member member) {
        CommentLike commentLike = CommentLike.createCommentLike(member, comment);
        commentRepository.saveCommentLike(commentLike);
        return commentLike.getCommentLikeId();
    }

    public Comment findOne(Long commentId) {
        Optional<Comment> comment = commentRepository.findOne(commentId);
        return comment.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment not found"));
    }

    public int commentLikeCount(Long commentId) {
        Comment comment = findOne(commentId);
        return comment.getLikeCount();
    }


    public Page<Comment> getMyComments(int page, Member member, String sortDirection) {
        Pageable pageable = PageRequest.of(page, CONTENT_PAGE_SIZE);
        if (sortDirection.equalsIgnoreCase("desc")) {
            return commentRepository.findMyComment(member, pageable, "desc");
        } else {
            return commentRepository.findMyComment(member, pageable, "asc");
        }
    }

    @Transactional
    public Long commentBookmarkAction(Comment comment, Member member) {
        CommentBookmark commentBookmark = CommentBookmark.createCommentBookmark(member, comment);
        commentRepository.saveCommentBookmark(commentBookmark);
        return commentBookmark.getCommentBookmarkId();
    }

    /**
     * 코멘트 등록
     */
    @Transactional
    public Comment registerComment(Member member, String content, Long postId) {

        LocalDateTime time = LocalDateTime.now();
        Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));

        Comment commentObj = Comment.createComment(post, member, content, time);
        commentRepository.saveComment(commentObj);

        //코멘트 - 피드백 동시관리
        commentFeedbackService.saveCommentFeedback(commentObj, null, ContentType.COMMENT, member, post, time);

        return commentObj;
    }

    /**
     * 코멘트 수정
     */
    @Transactional
    public void modifyComment(String comment, Long commentId) {
        Comment commentObj = findOne(commentId);
        commentObj.setContent(comment);

        // commentFeedbackService.findComment(commentObj).getComment().setContent(comment);
        commentFeedbackService.findComment(commentObj).setComment(commentObj);

        commentRepository.updateComment(commentObj);
    }


    @Transactional
    public void deleteComment(Long commentId) {
        Comment commentObj = findOne(commentId);

        CommentFeedback commentOfCommentFeedback = commentFeedbackService.findComment(commentObj);
        commentOfCommentFeedback.setDeleted(true);

        commentRepository.delete(commentObj);
    }


    public List<Comment> getRecentThreeComments(Post post) {
        PageRequest pageable = PageRequest.of(0, 3, Sort.by("commentedTime").descending());
        return commentRepository.findByPost(pageable, post).getContent();
    }


    public List<Comment> getParentCommentsReply(Comment parentComment) {
        return commentRepository.findRepliesOfParentComment(parentComment);
    }

    public Optional<CommentLike> findCommentLike(Member member, Comment comment) {
        return commentLikeRepository.findByMemberAndComment(member, comment);
    }

    @Transactional
    public void likeComment(Member member, Long commentId) {
        Comment commentObj = findOne(commentId);
        Integer commentLikeCount = commentObj.getLikeCount();

        // CommentLike를 db에서 조회해보고 조회 결과가 null이면 like+=1, CommentLike 엔티티 생성
        // null이 아니면 like -= 1, 조회결과인 해당 CommentLike 엔티티 삭제
        Optional<CommentLike> commentLike = findCommentLike(member, commentObj);
        if (commentLike.isEmpty()) {
            commentObj.setLikeCount(commentLikeCount + 1); // 좋아요 수 1 증가
            CommentLike commentLikeObj = CommentLike.createCommentLike(member, commentObj);
            commentLikeRepository.save(commentLikeObj);
        } else {
            commentObj.setLikeCount(commentLikeCount - 1); // 좋아요 수 1 차감
            commentLikeRepository.deleteById(commentLike.get().getCommentLikeId()); // db에서 삭제
        }
    }
}