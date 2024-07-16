package Remoa.BE.Web.Feedback.Service;


import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Feedback.Domain.FeedbackReply;
import Remoa.BE.Web.Feedback.Domain.FeedbackReplyLike;
import Remoa.BE.Web.Feedback.Repository.FeedbackMemberLogRepository;
import Remoa.BE.Web.Feedback.Repository.FeedbackReplyLikeRepository;
import Remoa.BE.Web.Feedback.Repository.FeedbackReplyRepository;
import Remoa.BE.Web.Feedback.Repository.FeedbackRepository;
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
public class FeedbackReplyService {

    private final PostRepository postRepository;
    private final FeedbackReplyRepository feedbackReplyRepository;
    private final FeedbackReplyLikeRepository feedbackReplyLikeRepository;
    private final FeedbackMemberLogRepository feedbackMemberLogRepository;


    @Transactional
    public void deleteByMember(Member member){
        feedbackReplyRepository.deleteByMember(member);
    }

    @Transactional
    public FeedbackReply registerFeedbackReply(Member member, Long postId, Long feedbackMemberLogId, String content) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));;
        FeedbackMemberLog feedbackMemberLog = feedbackMemberLogRepository.findById(feedbackMemberLogId).orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        FeedbackReply feedbackReply = FeedbackReply.createFeedbackReply(post, member, feedbackMemberLog, content);
        feedbackReplyRepository.save(feedbackReply);
        return feedbackReply;
    }

    @Transactional
    public void deleteByFeedbackMemberLog(FeedbackMemberLog feedbackMemberLog){
        feedbackReplyRepository.deleteByFeedbackMemberLog(feedbackMemberLog);
    }


    public List<FeedbackReply> findFeedbackReplies(FeedbackMemberLog feedbackMemberLog) {
        return feedbackReplyRepository.findByFeedbackMemberLogOrderByFeedbackReplyTimeAsc(feedbackMemberLog);
    }

    public FeedbackReply findOne(Long replyId) {
        return feedbackReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feedback reply not found"));
    }

    @Transactional
    public void modifyFeedbackReply(String feedbackReplyContent, Long feedbackReplyId) {
        FeedbackReply feedbackReply = feedbackReplyRepository.findById(feedbackReplyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feedback reply not found"));
        feedbackReply.setContent(feedbackReplyContent); // 변경 감지
    }

    @Transactional
    public void deleteFeedbackReply(Long feedbackReplyId) {
        FeedbackReply feedbackReply = feedbackReplyRepository.findById(feedbackReplyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feedback reply not found"));
        feedbackReplyRepository.delete(feedbackReply);
    }

    public Optional<FeedbackReplyLike> findFeedbackReplyLike(Member member, FeedbackReply feedbackReply) {
        return feedbackReplyLikeRepository.findByMemberAndFeedbackReply(member, feedbackReply);
    }

    @Transactional
    public void likeFeedbackReply(Member myMember, Long feedbackReplyId) {
        FeedbackReply feedbackReplyObj = findOne(feedbackReplyId);
        Integer feedbackReplyLikeCount = feedbackReplyObj.getLikeCount();

        // FeedbackReplyLike를 db에서 조회해보고 조회 결과가 null이면 like+=1, FeedbackReplyLike 엔티티 추가
        // null이 아니면 like -=1, 조회결과인 해당 FeedbackReplyLike 엔티티 삭제
        Optional<FeedbackReplyLike> feedbackReplyLike = findFeedbackReplyLike(myMember, feedbackReplyObj);
        if (feedbackReplyLike.isEmpty()) {
            feedbackReplyObj.setLikeCount(feedbackReplyLikeCount + 1); // 좋아요 수 1 증가
            FeedbackReplyLike feedbackReplyLikeObj = FeedbackReplyLike.createFeedbackReplyLike(myMember, feedbackReplyObj);
            feedbackReplyLikeRepository.save(feedbackReplyLikeObj);
        } else {
            feedbackReplyObj.setLikeCount(feedbackReplyLikeCount - 1); // 좋아요 수 1 차감
            feedbackReplyLikeRepository.deleteById(feedbackReplyLike.get().getFeedbackReplyLikeId()); // db에서 삭제
        }
    }

    public int feedbackReplyLikeCount(Long feedbackReplyId) {
        FeedbackReply feedbackReply = findOne(feedbackReplyId);
        return feedbackReply.getLikeCount();
    }

}
