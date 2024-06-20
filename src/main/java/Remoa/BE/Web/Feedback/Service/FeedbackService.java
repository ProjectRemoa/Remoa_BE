package Remoa.BE.Web.Feedback.Service;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackLike;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Feedback.Repository.FeedbackLikeRepository;
import Remoa.BE.Web.Feedback.Repository.FeedbackRepository;
import Remoa.BE.Web.Member.Domain.*;
import Remoa.BE.Web.CommentFeedback.Service.CommentFeedbackService;
import Remoa.BE.Web.Post.Repository.PostRepository;
import Remoa.BE.Web.Post.Service.PostService;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackLikeRepository feedbackLikeRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final CommentFeedbackService commentFeedbackService;

    @Transactional
    public Feedback findOne(Long feedbackId) {
        Optional<Feedback> feedback = feedbackRepository.findOne(feedbackId);
        return feedback.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feedback not found"));
    }

    public Page<Feedback> findMyFeedback(int page, Member member, String sortDirection) {
        Pageable pageable = PageRequest.of(page, CONTENT_PAGE_SIZE);
        if (sortDirection.equalsIgnoreCase("asc")) {
            return feedbackRepository.findOldestFeedback(member, pageable);
        } else {
            return feedbackRepository.findNewestFeedback(member, pageable);
        }
    }

    public int feedbackLikeCount(Long feedbackId) {
        Feedback feedback = findOne(feedbackId);
        return feedback.getLikeCount();
    }

    public List<Feedback> findAllFeedbacksOfPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        ;
        return feedbackRepository.findByPost(post);
    }


    public Optional<FeedbackLike> findFeedbackLike(Member member, Feedback feedback) {
        return feedbackLikeRepository.findByMemberAndFeedback(member, feedback);
    }

    @Transactional
    public void registerFeedback(Member member, String content, Long postId, Integer pageNumber) {


        Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        ;

        LocalDateTime time = LocalDateTime.now();

        Feedback feedbackObj = Feedback.createFeedback(post, member, pageNumber, content, time);

        feedbackRepository.saveFeedback(feedbackObj);

        commentFeedbackService.saveCommentFeedback(null, feedbackObj, ContentType.FEEDBACK, member, post, time);

    }

    @Transactional
    public void modifyFeedback(String content, Long feedbackId) {
        Feedback feedbackObj = findOne(feedbackId);
        feedbackObj.setContent(content);

        //commentFeedbackService.findFeedback(feedbackObj).getFeedback().setContent(content);
        commentFeedbackService.findFeedback(feedbackObj).setFeedback(feedbackObj);

        feedbackRepository.updateFeedback(feedbackObj);
    }

    @Transactional
    public void deleteFeedback(Long feedbackId) {
        Feedback feedbackObj = findOne(feedbackId);

        CommentFeedback feedbackOfCommentFeedback = commentFeedbackService.findFeedback(feedbackObj);
        feedbackOfCommentFeedback.setDeleted(true);
        feedbackRepository.delete(feedbackObj);
    }

    @Transactional
    public void likeFeedback(Member myMember, Long feedbackId) {
        Feedback feedbackObj = findOne(feedbackId);
        Integer feedbackLikeCount = feedbackObj.getLikeCount();

        //FeedbackLike를 db에서 조회해보고 조회 결과가 null이면 like+=1, FeedbackLike 엔티티 추가
        // null이 아니면 like -=1, 조회결과인 해당 FeedbackLike 엔티티 삭제
        Optional<FeedbackLike> feedbackLike = findFeedbackLike(myMember, feedbackObj);
        if (feedbackLike.isEmpty()) {
            feedbackObj.setLikeCount(feedbackLikeCount + 1); // 좋아요 수 1 증가
            FeedbackLike feedbackLikeObj = FeedbackLike.createFeedbackLike(myMember, feedbackObj);
            feedbackLikeRepository.save(feedbackLikeObj);
        } else {
            feedbackObj.setLikeCount(feedbackLikeCount - 1); // 좋아요 수 1 차감
            feedbackLikeRepository.deleteById(feedbackLike.get().getFeedbackLikeId()); // db에서 삭제
        }
    }
}