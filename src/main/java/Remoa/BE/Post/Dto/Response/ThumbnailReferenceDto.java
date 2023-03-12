package Remoa.BE.Post.Dto.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 작업물 목록을 보여줄 때 쓰일 Post의 간단한 정보만을 담은 Dto.
 * 레퍼런스들을 조회해서 썸네일을 보여주는 모든 페이지에서 사용 가능.
 */
@Data
@Builder
public class ThumbnailReferenceDto {

    //TODO 썸네일로 쓰일 Member의 profileImage 필요함. -> 광휘님 작업 왼료되면 추가 예정

    public Long postId;
    public Long memberMemberId;
    public String nickname;
    public String title;
    public Integer likeCount;
    public String postingTime;
    public Integer views;
    public Integer scrapCount;
    public List<String> storeFileUrls;
    public String categoryName;

}
