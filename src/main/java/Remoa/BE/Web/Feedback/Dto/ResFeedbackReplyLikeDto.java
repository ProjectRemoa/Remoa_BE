package Remoa.BE.Web.Feedback.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResFeedbackReplyLikeDto {
    @Schema(description = "좋아요 수", example = "21")
    private int likeCount;
}
