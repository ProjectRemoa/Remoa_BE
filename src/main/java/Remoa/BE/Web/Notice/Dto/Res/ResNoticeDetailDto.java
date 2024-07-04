package Remoa.BE.Web.Notice.Dto.Res;


import Remoa.BE.Web.Notice.domain.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class ResNoticeDetailDto {


    @Schema(description = "공지 ID", example = "1")
    private Long noticeId;

    @Schema(description = "작성자", example = "관리자")
    private String author;

    @Schema(description = "제목", example = "중요한 공지 제목")
    private String title;

    @Schema(description = "내용", example = "이것은 중요한 공지 내용 입니다....")
    private String content;

    @Schema(description = "작성일", example = "2024-04-06")
    private LocalDate postingTime;

    @Schema(description = "조회 수", example = "100")
    private int view;

    @Schema(description = "수정 여부", example = "true")
    private boolean modified; // 수정 여부 표시

    @Schema(description = "수정 시각", example = "2024-04-08T10:30:00")
    private LocalDateTime modifiedTime; // 수정 시각

    public ResNoticeDetailDto(Notice entity) {
        this.noticeId = entity.getNoticeId();
        this.author = entity.getAuthor();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.postingTime = entity.getPostingTime().toLocalDate();
        this.view = entity.getView();
        this.modified = entity.getModified();
        this.modifiedTime = entity.getModifiedTime();
    }
}
