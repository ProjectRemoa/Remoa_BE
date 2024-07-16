package Remoa.BE.Web.Member;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Comment.Domain.CommentReply;
import Remoa.BE.Web.Comment.Dto.Res.ResCommentDto;
import Remoa.BE.Web.Comment.Dto.Res.ResCommentReplyDto;
import Remoa.BE.Web.Comment.Service.CommentReplyService;
import Remoa.BE.Web.Comment.Service.CommentService;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackReply;
import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Feedback.Dto.ResFeedbackDto2;
import Remoa.BE.Web.Feedback.Dto.ResFeedbackInfoDto;
import Remoa.BE.Web.Feedback.Dto.ResFeedbackReplyDto;
import Remoa.BE.Web.Feedback.Repository.FeedbackMemberLogRepository;
import Remoa.BE.Web.Feedback.Service.FeedbackReplyService;
import Remoa.BE.Web.Feedback.Service.FeedbackService;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Res.ResMemberInfoDto;
import Remoa.BE.Web.Member.Service.FollowService;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberUtils {

    private final FollowService followService;
    private final PostService postService;
    private final CommentService commentService;
    private final CommentReplyService commentReplyService;
    private final FeedbackService feedbackService;
    private final FeedbackReplyService feedbackReplyService;


    public Boolean isMyMemberFollowMember(Member myMember, Member toMember) {
        return myMember != null ? followService.isMyMemberFollowMember(myMember, toMember) : null;
    }

    public Boolean isLikedPost(Member myMember, Post post) {
        return myMember != null && postService.isThisPostLiked(myMember, post);
    }

    public Boolean isScrapedPost(Member myMember, Post post) {
        return myMember != null && postService.isThisPostScraped(myMember, post);
    }

    private Boolean isLikedComment(Member myMember, Comment comment) {
        return myMember != null && commentService.findCommentLike(myMember, comment).isPresent();
    }

    private Boolean isLikedCommentReply(Member myMember, CommentReply commentReply) {
        return myMember != null && commentReplyService.findCommentReplyLike(myMember, commentReply).isPresent();
    }

    /**
     * 내가 피드백 단 유저에 좋아요를 눌렀는지 여부
     */
    private Boolean isLikedFeedbackMember(Member myMember, FeedbackMemberLog feedbackMemberLog) {
        return myMember != null && feedbackService.findFeedbackMemberLike(myMember, feedbackMemberLog).isPresent();
    }

    private Boolean isLikedFeedbackReply(Member myMember, FeedbackReply feedbackReply) {
        return myMember != null && feedbackReplyService.findFeedbackReplyLike(myMember, feedbackReply).isPresent();
    }

    // 추가적인 유틸리티 메서드들...

    public List<ResFeedbackDto2> feedbackList(Long postId, Member myMember) {
        Post post = postService.findOne(postId);
        List<Feedback> feedbacks = feedbackService.findAllFeedbacksOfPost(postId);

        // 피드백을 멤버별로 그룹화
        Map<Member, List<Feedback>> feedbacksByMember = feedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getMember));

        // 멤버별 피드백을 첫 번째 피드백의 작성 시간 기준으로 정렬
        List<Map.Entry<Member, List<Feedback>>> sortedFeedbacksByMember = feedbacksByMember.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().get(0).getFeedbackTime()))
                .toList();

        // 각 그룹 내의 피드백을 pageNumber 순으로 정렬
        sortedFeedbacksByMember.forEach(entry ->
                entry.setValue(entry.getValue().stream()
                        .sorted(Comparator.comparing(Feedback::getPageNumber))
                        .collect(Collectors.toList()))
        );

        List<ResFeedbackDto2> resFeedbackDtos = sortedFeedbacksByMember.stream()
                .map(entry -> {
                    Member feedbackMember = entry.getKey(); // 피드백 작성자
                    List<Feedback> memberFeedbacks = entry.getValue();

                    List<ResFeedbackInfoDto> feedbackInfos = memberFeedbacks.stream()
                            .map(ResFeedbackInfoDto::new)
                            .collect(Collectors.toList());

                    ResMemberInfoDto memberInfoDto = new ResMemberInfoDto(feedbackMember, isMyMemberFollowMember(myMember, feedbackMember));
                    FeedbackMemberLog feedbackMemberLog = feedbackService.findFeedbackMemberLog(feedbackMember, post);

                    return new ResFeedbackDto2(memberInfoDto,
                            feedbackMemberLog,
                            isLikedFeedbackMember(myMember, feedbackMemberLog),
                            feedbackInfos,
                            feedbackReplyService.findFeedbackReplies(feedbackMemberLog).stream().map(
                                    feedbackReply -> new ResFeedbackReplyDto(
                                            feedbackReply,
                                            isLikedFeedbackReply(myMember, feedbackReply),
                                            isMyMemberFollowMember(myMember, feedbackReply.getMember()))).collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());

        return resFeedbackDtos;
    }


    public List<ResCommentDto> commentList(Long postId, Member myMember) {

        List<Comment> comments = commentService.findAllCommentsOfPost(postId);
        List<ResCommentDto> resCommentDtos = comments.stream()
                .map(comment -> {
                    List<CommentReply> replies = commentReplyService.findCommentReplies(comment);
                    List<ResCommentReplyDto> resReplies = replies.stream()
                            .map(reply -> new ResCommentReplyDto(reply,
                                    isLikedCommentReply(myMember, reply),
                                    isMyMemberFollowMember(myMember, reply.getMember())))
                            .collect(Collectors.toList());

                    return new ResCommentDto(comment,
                            isLikedComment(myMember, comment),
                            isMyMemberFollowMember(myMember, comment.getMember()),
                            resReplies);
                })
                .toList();

        return resCommentDtos;
    }
}
