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
public class ResInquiryDetailDto {

    @Schema(description = "문의 ID", example = "1")
    private Long inquiryId;

    @Schema(description = "작성자", example = "Alice Smith")
    private String author;

    @Schema(description = "제목", example = "Product Inquiry")
    private String title;

    @Schema(description = "내용", example = "I have some questions regarding your product...")
    private String content;

    @Schema(description = "작성일", example = "2024-04-06")
    private LocalDate postingTime;

    @Schema(description = "조회 수", example = "50")
    private int view;

    @Schema(description = "답변 여부", example = "true")
    private Boolean isReplied;

    @Schema(description = "수정 여부", example = "true")
    private boolean modified; // 수정 여부 표시

    @Schema(description = "수정 시각", example = "2024-04-08T10:30:00")
    private LocalDateTime modifiedTime; // 수정 시각


    public ResInquiryDetailDto(Inquiry entity) {
        this.inquiryId = entity.getInquiryId();
        this.author = entity.getAuthor();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.postingTime = entity.getPostingTime().toLocalDate();
        this.view = entity.getView();
        this.isReplied = entity.getReplied();
        this.modified = entity.getModified();
        this.modifiedTime = entity.getModifiedTime();
    }
}
