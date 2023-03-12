package Remoa.BE.Post.Dto.Response;

import lombok.Data;

@Data
public class ThumbnailFeedbackDto {

    //TODO 썸네일로 쓰일 Member의 profileImage 필요함. -> 광휘님 작업 왼료되면 추가 예정

    public Long postId;
    public String title;
    public Long commentMemberId;
    public String commentNickname;
    public String comment;
    public Long replyMemberId;
    public String replyNickname;
    public String reply;
    public Integer commentLikeCount;
    public Integer replyLikeCount;
    public String postingTime;

}
