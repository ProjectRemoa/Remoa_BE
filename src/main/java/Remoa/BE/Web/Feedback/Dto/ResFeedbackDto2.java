package Remoa.BE.Web.Feedback.Dto;


import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Domain.FeedbackMemberLog;
import Remoa.BE.Web.Member.Dto.Res.ResMemberInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ResFeedbackDto2 {

    @Schema(description = "피드백 작성자 정보")
    private ResMemberInfoDto member;

    @Schema(description = "피드백-회원 로깅 ID")
    private Long feedbackMemberLogId;

    @Schema(description = "좋아요 수", example = "10")
    private Integer likeCount;

    @Schema(description = "현재 사용자가 피드백 작성자를 좋아하는지 여부", example = "true")
    private Boolean isLiked;

    @Schema(description = "해당 회원의 피드백 정보")
    private List<ResFeedbackInfoDto> feedbackInfos;

    @Schema(description = "피드백에 대한 답글 목록")
    private List<ResFeedbackReplyDto> replies;


    public ResFeedbackDto2(ResMemberInfoDto member,
                           FeedbackMemberLog feedbackMemberLog,
                           Boolean isLiked,
                           List<ResFeedbackInfoDto> feedbackInfos,
                           List<ResFeedbackReplyDto> replies) {
        this.member = member;
        this.feedbackMemberLogId = feedbackMemberLog.getFeedbackMemberLogId();
        this.likeCount = feedbackMemberLog.getLikeCount();
        this.isLiked = isLiked;
        this.feedbackInfos = feedbackInfos;
        this.replies = replies;
    }
}
