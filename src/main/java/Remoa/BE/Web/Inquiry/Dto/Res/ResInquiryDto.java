package Remoa.BE.Web.Inquiry.Dto.Res;

import Remoa.BE.Web.Inquiry.Domain.Inquiry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class ResInquiryDto {

    @Schema(description = "문의 ID", example = "1")
    private Long id;

    @Schema(description = "작성자", example = "Alice Smith")
    private String author;

    @Schema(description = "제목", example = "Product Inquiry")
    private String title;

    @Schema(description = "작성일", example = "2024-04-06")
    private LocalDate postingTime;

    @Schema(description = "답변 여부", example = "true")
    private Boolean isReplied;

    @Schema(description = "조회 수", example = "50")
    private int view;

    @Schema(description = "수정 여부", example = "true")
    private boolean modified; // 수정 여부 표시

    @Schema(description = "수정 시각", example = "2024-04-08T10:30:00")
    private LocalDateTime modifiedTime; // 수정 시각

    public ResInquiryDto(Inquiry inquiry) {
        this.id = inquiry.getInquiryId();
        this.author = inquiry.getAuthor();
        this.title = inquiry.getTitle();
        this.postingTime = inquiry.getPostingTime().toLocalDate();
        this.isReplied = inquiry.getReplied();
        this.view = inquiry.getView();
        this.modified = inquiry.getModified();
        this.modifiedTime = inquiry.getModifiedTime();

    }

}
