package Remoa.BE.Web.Feedback.Service;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackLike;
import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Feedback.Repository.FeedbackMemberLogRepository;
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
import java.util.Objects;
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
    private final CommentFeedbackService commentFeedbackService;
    private final FeedbackMemberLogRepository feedbackMemberLogRepository;

    @Transactional
    public Feedback findOne(Long feedbackId) {
        Optional<Feedback> feedback = feedbackRepository.findOne(feedbackId);
        return feedback.orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
    }

    public Page<Feedback> getMyFeedback(int page, Member member, String sortDirection) {
        Pageable pageable = PageRequest.of(page, CONTENT_PAGE_SIZE);
        if (sortDirection.equalsIgnoreCase("desc")) {
            return feedbackRepository.findMyFeedback(member, pageable, "desc");
        } else {
            return feedbackRepository.findMyFeedback(member, pageable, "asc");
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


    public Optional<FeedbackLike> findFeedbackMemberLike(Member member, FeedbackMemberLog feedbackMemberLog) {
        return feedbackLikeRepository.findByMemberAndFeedbackMemberLog(member, feedbackMemberLog);
    }

    public FeedbackMemberLog findFeedbackMemberLog(Member member, Post post) {
        return feedbackMemberLogRepository.findByMemberAndPost(member, post)
                .orElseThrow(() -> new BaseException(CustomMessage.POST_MEMBER_FEEDBACK_NOT_EXIST));
    }

    @Transactional
    public void registerFeedback(Member member, String content, Long postId, Integer pageNumber) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));

        if (feedbackRepository.existsByMemberAndPostAndPageNumber(member, post, pageNumber)) {
            throw new BaseException(CustomMessage.PAGE_FEEDBACK_ALREADY_EXISTS);
        }

        if (!feedbackMemberLogRepository.existsByMemberAndPost(member, post)) { // 포스트멤버피드백 없으면 등록
            feedbackMemberLogRepository.save(new FeedbackMemberLog(member, post));
        }

        LocalDateTime time = LocalDateTime.now();
        Feedback feedbackObj = Feedback.createFeedback(post, member, pageNumber, content, time);

        feedbackRepository.saveFeedback(feedbackObj);

        commentFeedbackService.saveCommentFeedback(null, feedbackObj, ContentType.FEEDBACK, member, post, time);

    }

    @Transactional
    public void modifyFeedback(Member member, Post post, String content, Long feedbackId) {
        Feedback feedbackObj = findOne(feedbackId);
        if (!Objects.equals(feedbackObj.getMember().getMemberId(), member.getMemberId())) { // 자신이 적은 피드백 여부 확인
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }

        feedbackObj.setContent(content);
        commentFeedbackService.findFeedback(feedbackObj).setFeedback(feedbackObj);

   //     feedbackRepository.updateFeedback(feedbackObj); //알아서 수정 됨
    }

    @Transactional
    public void deleteFeedback(Member member, Post post, Long feedbackId) {
        Feedback feedbackObj = findOne(feedbackId);

        if (!Objects.equals(feedbackObj.getMember().getMemberId(), member.getMemberId())) { // 자신이 적은 피드백 여부 확인
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }

        CommentFeedback feedbackOfCommentFeedback = commentFeedbackService.findFeedback(feedbackObj);
        feedbackOfCommentFeedback.setDeleted(true);

        feedbackRepository.delete(feedbackObj);
        if (feedbackRepository.existsByMemberAndPost(member, post)) { //더이상 해당 포스트에 작성한 피드백 존재하지 않는다면
            feedbackMemberLogRepository.deleteByMemberAndPost(member, post); // 포스트멤버피드백로그 삭제
        }
    }

    @Transactional
    public int likeFeedback(Member myMember, Post post , Member feedbackMember) {

        FeedbackMemberLog feedbackMemberLog = feedbackMemberLogRepository.findByMemberAndPost(feedbackMember, post)
                .orElseThrow(() -> new BaseException(CustomMessage.POST_MEMBER_FEEDBACK_NOT_EXIST)); //포스트에 피드백 없으면 에러

        //FeedbackLike를 db에서 조회해보고 조회 결과가 null이면 like+=1, FeedbackLike 엔티티 추가
        // null이 아니면 like -=1, 조회결과인 해당 FeedbackLike 엔티티 삭제
        Optional<FeedbackLike> feedbackLike = findFeedbackMemberLike(myMember, feedbackMemberLog);
        if (feedbackLike.isEmpty()) {
            FeedbackLike feedbackLikeObj = FeedbackLike.createFeedbackLike(myMember, feedbackMemberLog); // 좋아요 생성
            feedbackMemberLog.increaseLikeCount(); // 대상 피드백 멤버 좋아요 수 1 증가
            feedbackLikeRepository.save(feedbackLikeObj);
            return feedbackMemberLog.getLikeCount();
        } else {
            feedbackMemberLog.decreaseLikeCount();    // 좋아요 수 1 차감
            feedbackLikeRepository.deleteById(feedbackLike.get().getFeedbackLikeId()); // db에서 삭제
            return feedbackMemberLog.getLikeCount();
        }
    }
}