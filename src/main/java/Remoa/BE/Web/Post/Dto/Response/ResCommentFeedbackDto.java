package Remoa.BE.Web.Post.Dto.Response;

import Remoa.BE.Web.Member.Dto.Res.ResMemberInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 내 활동 관리에 쓰이는 Comment와 Feedback을 구분 없이 최신순으로 볼러오는 데 쓰이는 dto.
 */
@Data
@Builder
public class ResCommentFeedbackDto {

    @Schema(description = "게시물 제목", example = "서울 빅데이터 공모전 최우수상")
    private String title;

    @Schema(description = "게시물 ID", example = "2")
    private Long postId;

    @Schema(description = "코멘트 ID")
    private Long commentId;

    @Schema(description = "피드백 ID")
    private Long feedbackId;

    @Schema(description = "썸네일 URL", example = "https://remoa.s3.ap-northeast-2.amazonaws.com/thumbnail/3d6655e8-d922-4da4-aeac-5edc2a5cc0ca_basic_profile.png")
    private String thumbnail;

    @Schema(description = "게시물 작성자 정보")
    private ResMemberInfoDto member;

    @Schema(description = "코멘트/피드백 내용", example = "잘했어요")
    private String content;

    @Schema(description = "좋아요 수", example = "0")
    private Integer likeCount;

    @Schema(description = "코멘트인지 여부", example = "false")
    private Boolean isComment;

}
