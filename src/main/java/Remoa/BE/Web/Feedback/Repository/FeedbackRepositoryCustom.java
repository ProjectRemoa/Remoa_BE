package Remoa.BE.Web.Feedback.Repository;

import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackLike;
import Remoa.BE.Web.Member.Domain.FeedbackBookmark;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepositoryCustom {

    Optional<Feedback> findOne(Long id);

    void saveFeedback(Feedback feedback);

    Optional<Feedback> findByFeedbackId(Long feedbackId);

    List<Feedback> findByPost(Post post);

    List<Feedback> findRepliesOfParentFeedback(Feedback parentFeedback);

    void saveFeedbackLike(FeedbackLike feedbackLike);

    Optional<FeedbackLike> findMemberCommendLike(Member member, Feedback feedback);

    Integer findFeedbackLike(Feedback feedback);

    void saveFeedbackBookmark(FeedbackBookmark feedbackBookmark);

    Optional<FeedbackBookmark> findMemberCommendBookmark(Member member, Feedback feedback);

    void updateFeedback(Feedback newFeedback);

    void deleteFeedback(Feedback feedback);

    List<Feedback> findAllByMember(Member member);



    void deleteChildFeedbackByParentFeedback(Feedback feedback);

    Page<Feedback> findMyFeedback(Member member, Pageable pageable, String sort);
}
