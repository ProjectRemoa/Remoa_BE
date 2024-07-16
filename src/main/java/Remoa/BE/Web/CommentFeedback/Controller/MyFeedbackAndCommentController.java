package Remoa.BE.Web.CommentFeedback.Controller;

import Remoa.BE.Web.Comment.Service.CommentReplyService;
import Remoa.BE.Web.Comment.Service.CommentService;
import Remoa.BE.Web.CommentFeedback.Repository.CommentFeedbackRepository;
import Remoa.BE.Web.CommentFeedback.Service.CommentFeedbackService;
import Remoa.BE.Web.Member.MemberUtils;
import Remoa.BE.Web.Member.Service.FollowService;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Post.Service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "받은 피드백 기능 ?? 쓰이는지 ?", description = "받은 피드백 기능 API")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MyFeedbackAndCommentController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;
    private final CommentFeedbackService commentFeedbackService;
    private final CommentFeedbackRepository commentFeedbackRepository;
    private final FollowService followService;
    private final CommentReplyService commentReplyService;
    private final MemberUtils memberUtils;

   /* @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 받은 피드백과 코멘트를 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/feedback")
    @Operation(summary = "내가 받은 피드백 코멘트 조회", description = "내가 받은 최신 피드백 코멘트들을 조회합니다.")
    public ResponseEntity<Object> receivedFeedback(HttpServletRequest request,
                                                   @RequestParam(required = false, defaultValue = "all") String category,
                                                   @RequestParam(required = false, defaultValue = "1", name = "page") int pageNumber) {

        if (authorized(request)) {
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);

            pageNumber -= 1;
            if (pageNumber < 0) {
                return errorResponse(CustomMessage.PAGE_NUM_OVER);
            }

//            Page<Post> posts = myPostService.getNewestThreePosts(pageNumber, myMember);
            Page<Post> posts;
            if (categoryList.contains(category)) {

                posts = myPostService.getNewestThreePostsSortCategory(pageNumber, myMember, category);

            } else {
                posts = myPostService.getNewestThreePosts(pageNumber, myMember);
            }


            if ((posts.getContent().isEmpty()) && (posts.getTotalElements() > 0)) {
                return errorResponse(CustomMessage.PAGE_NUM_OVER);
            }

            Map<String, Object> result = new HashMap<>();
            List<Object> res = new ArrayList<>();

            for (Post post : posts) { //조회한 post
                Map<String, ResCommentDto> commentInfo = new HashMap<>();

                List<Comment> parentComments = commentService.getRecentThreeCommentsExceptReply(post);

                int commentNumber = 1;
                for (Comment parentComment : parentComments) { //조회한 post의 parent comment
                    List<Comment> parentCommentsReply = commentService.getParentCommentsReply(parentComment);



                    List<ResReplyDto> replies = new ArrayList<>();
                    for (Comment reply : parentCommentsReply) {

                        replies.add(new ResReplyDto(
                                reply.getCommentId(),
                                new ResMemberInfoDto(reply.getMember().getMemberId(),
                                        reply.getMember().getNickname(),
                                        reply.getMember().getProfileImage(),
                                        followService.isMyMemberFollowMember(myMember, reply.getMember())),
                                reply.getComment(),
                                reply.getCommentLikeCount(),
                                commentService.findCommentLike(myMember.getMemberId(), reply.getCommentId()).isPresent(),
                                reply.getCommentedTime()));
                    }

                    commentInfo.put("comment_" + commentNumber, new ResCommentDto(
                            parentComment.getCommentId(),
                            new ResMemberInfoDto(parentComment.getMember().getMemberId(),
                                    parentComment.getMember().getNickname(),
                                    parentComment.getMember().getProfileImage(),
                                    followService.isMyMemberFollowMember(myMember, parentComment.getMember())),
                            parentComment.getComment(),
                            parentComment.getCommentLikeCount(),
                            commentService.findCommentLike(myMember.getMemberId(),
                                    parentComment.getCommentId()).isPresent(),
                            parentComment.getCommentedTime(),
                            replies));

                    commentNumber++;
                }

                ResReceivedCommentDto map = ResReceivedCommentDto.builder()
                        .title(post.getTitle())
                        .thumbnail(post.getThumbnailUrl())
                        .postId(post.getPostId())
                        .commentInfo(commentInfo)
                        .build();

                res.add(map);

            }
            result.put("post",res);
            result.put("totalPages", posts.getTotalPages()); //전체 페이지의 수
            result.put("totalOfAllComments", posts.getTotalElements()); //모든 코멘트의 수
            result.put("totalOfPageElements", posts.getNumberOfElements()); //현 페이지 피드백의 수

            return successResponse(CustomMessage.OK, result);

        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }*/
}
