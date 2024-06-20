package Remoa.BE.Web.Comment.Dto.Res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResCommentReplyLikeDto {
    @Schema(description = "좋아요 수", example = "21")
    private int likeCount;

}
