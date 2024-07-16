package Remoa.BE.Web.Feedback.Dto;

import Remoa.BE.Web.Feedback.Domain.Feedback;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResFeedbackInfoDto {

    @Schema(description = "피드백 ID", example = "123")
    private Long feedbackId;

    @Schema(description = "피드백 내용", example = "좋은 정보 감사합니다.")
    private String feedback;

    @Schema(description = "페이지 번호", example = "1")
    private Integer page;

    @Schema(description = "삭제여부", example = "false")
    private Boolean isDeleted;

    @Schema(description = "피드백 작성 시간", example = "2024-04-05T08:30:00")
    private LocalDateTime feedbackTime;



    public ResFeedbackInfoDto(Feedback feedback) {
        this.feedbackId = feedback.getFeedbackId();
        this.feedback = feedback.getContent();
        this.page = feedback.getPageNumber();
        this.isDeleted = feedback.getDeleted();
        this.feedbackTime = feedback.getFeedbackTime();

    }
}
