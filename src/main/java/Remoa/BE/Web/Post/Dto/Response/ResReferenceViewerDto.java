package Remoa.BE.Web.Post.Dto.Response;

import Remoa.BE.Web.Comment.Dto.Res.ResCommentDto;
import Remoa.BE.Web.Feedback.Dto.ResFeedbackDto2;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Res.ResMemberInfoDto;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Domain.UploadFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "레퍼런스 뷰어 응답 DTO")
@Data
public class ResReferenceViewerDto {

    @Schema(description = "게시물 ID", example = "1")
    private Long postId;

    @Schema(description = "게시물 작성자 정보")
    private ResMemberInfoDto postMember;

    @Schema(description = "게시물 썸네일 이미지 URL")
    private String thumbnail;

    @Schema(description = "공모전명", example = "서울 빅데이터 공모전")
    private String contestName;

    @Schema(description = "수상 유형", example = "최우수상")
    private String contestAwardType;

    @Schema(description = "카테고리", example = "etc")
    private String category;

    @Schema(description = "게시물 제목", example = "서울 빅데이터 공모전 최우수상")
    private String title;

    @Schema(description = "게시물 좋아요 수", example = "1")
    private Integer likeCount;

    @Schema(description = "내가 게시물을 좋아요 했는지 여부", example = "true")
    private Boolean isLiked;

    @Schema(description = "게시물 스크랩 수", example = "0")
    private Integer scrapCount;

    @Schema(description = "내가 게시물을 스크랩했는지 여부", example = "false")
    private Boolean isScraped;

    @Schema(description = "게시물 작성 시간", example = "2023-03-28T10:45:26")
    private String postingTime;

    @Schema(description = "게시물 조회 수", example = "4")
    private Integer views;

    @Schema(description = "페이지 수", example = "1")
    private Integer pageCount;

    @Schema(description = "YouTube 링크")
    private String youtubeLink;

    @Schema(description = "첨부 파일명 목록")
    private List<String> fileNames;

    @Schema(description = "게시물 댓글 목록")
    private List<ResCommentDto> comments;

    @Schema(description = "게시물 피드백 목록")
    private List<ResFeedbackDto2> feedbacks;

    public ResReferenceViewerDto(Post post, Member postMember, Boolean isFollow, Boolean isLiked, Boolean isScraped, List<ResCommentDto> comments, List<ResFeedbackDto2> feedbacks) {
        this.postId = post.getPostId();
        this.postMember = new ResMemberInfoDto(postMember, isFollow);
        this.thumbnail = post.getThumbnailUrl();
        this.contestName = post.getContestName();
        this.contestAwardType = post.getContestAwardType();
        this.category = post.getCategory().getName();
        this.title = post.getTitle();
        this.likeCount = post.getLikeCount();
        this.isLiked = isLiked;
        this.scrapCount = post.getScrapCount();
        this.isScraped = isScraped;
        this.postingTime = post.getPostingTime().toString();
        this.views = post.getViews();
        this.pageCount = post.getPageCount();
        this.youtubeLink = post.getYoutubeLink();
        this.fileNames = post.getUploadFiles() != null ?
                post.getUploadFiles().stream().map(UploadFile::getStoreFileUrl).collect(Collectors.toList()) : null;
        this.comments = comments;
        this.feedbacks = feedbacks;
    }
}
