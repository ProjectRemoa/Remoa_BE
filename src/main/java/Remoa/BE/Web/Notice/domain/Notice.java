package Remoa.BE.Web.Notice.domain;

import Remoa.BE.Web.Notice.Dto.Req.ReqNoticeDto;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE notice SET deleted = true WHERE notice_id = ?")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    private String author;

    private String title;

    private String content;

    private LocalDateTime postingTime;

    private int view;

    @Builder.Default
    private Boolean modified = Boolean.FALSE; // 수정 여부 표시

    private LocalDateTime modifiedTime; // 수정 시각

    @Builder.Default
    private Boolean deleted = Boolean.FALSE;

    public void addViewCount() {
        this.view++;
    }

    public void updateNotice(ReqNoticeDto updateDto, String author) {
        this.title = updateDto.getTitle();
        this.content = updateDto.getContent();
        this.author = author;
        this.modified = Boolean.TRUE; // 수정됨을 표시
        this.modifiedTime = LocalDateTime.now(); // 현재 시각으로 수정 시각 업데이트
        // 여기에 필요한 필드를 업데이트하는 코드를 추가할 수 있습니다.
    }
}
