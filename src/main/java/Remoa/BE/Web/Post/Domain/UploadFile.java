package Remoa.BE.Web.Post.Domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "FILE")
@SQLRestriction("deleted = false")
public class UploadFile {

    @Id
    @GeneratedValue
    private Long uploadFileId;


    /**
     * 업로드된 파일의 원본 이름
     */
    @Column(name = "original_file_name")
    private String originalFileName;

    /**
     * UUID.randomUUID()를 통해 받은 랜덤값을 통해 S3파일 서버에 저장할 파일의 이름
     */
    @Column(name = "save_file_name", length = 500)
    private String saveFileName;

    /**
     * 파일의 확장자
     */
    private String extension;

    /**
     * 파일이 저장된 시간 저장
     * Lob 는 긴 문자열을 처리해 줍니다.
     */
    @Lob
    @Column(name = "store_file_url", length = 500)
    private String storeFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private Boolean deleted = Boolean.FALSE;

    //이후 업로드 날짜 및 시간, 컨텐츠 타입, 사이즈 등의 필드등이 필요할 때 손봐야할듯.
}
