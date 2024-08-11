package Remoa.BE.Web.MyPage.Controller;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Comment.Service.CommentService;
import Remoa.BE.Web.CommentFeedback.Domain.CommentFeedback;
import Remoa.BE.Web.CommentFeedback.Dto.ResReceivedCommentFeedbackDto;
import Remoa.BE.Web.CommentFeedback.Service.CommentFeedbackService;
import Remoa.BE.Web.Feedback.Domain.Feedback;
import Remoa.BE.Web.Feedback.Service.FeedbackService;
import Remoa.BE.Web.Member.Domain.ContentType;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Res.ResMemberInfoDto;
import Remoa.BE.Web.Member.Service.FollowService;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.MyPage.Dto.Res.*;
import Remoa.BE.Web.Post.Domain.PostScrap;
import Remoa.BE.Web.Post.Dto.Response.*;
import Remoa.BE.Web.Post.Service.PostService;
import Remoa.BE.config.auth.MemberDetails;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import Remoa.BE.exception.response.BaseResponse;
import Remoa.BE.exception.response.ErrorResponse;
import Remoa.BE.utill.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "내 활동 기능 Test Completed", description = "내 활동 기능 API")
@RestController
@Slf4j
@RequiredArgsConstructor
public class MyActivityController {

    private final MemberService memberService;
    private final CommentFeedbackService commentFeedbackService;
    private final PostService postService;
    private final FollowService followService;
    private final CommentService commentService;
    private final FeedbackService feedbackService;

    /**
     * 내 활동 관리
     *
     * @param
     * @return Map<String, Object>
     * "contents" : 내가 작성한 최신 댓글(Comment, Feedback 무관 1개)
     * "posts" : 내가 스크랩한 post들을 가장 최근 스크랩한 순서의 List(12개).
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 활동을 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/activity")
    @Operation(summary = "내 활동 조회 Test Completed", description = "내가 작성한 최신 코멘트/피드백(Comment, Feedback 무관 1개)와 스크랩한 게시물들을 조회합니다.")
    public ResponseEntity<BaseResponse<ResMyActivityDto>> myActivity(@AuthenticationPrincipal MemberDetails memberDetails) {

        log.info("EndPoint Get /user/activity");
        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);

        ResMyActivityDto result = new ResMyActivityDto();

        CommentFeedback commentFeedback = commentFeedbackService.findNewestCommentFeedback(myMember);

        ResCommentFeedbackDto commentOrFeedback = null;
        if (commentFeedback != null && commentFeedback.getType().equals(ContentType.FEEDBACK)) {
            commentOrFeedback = feedbackBuilder(commentFeedback);
        } else if (commentFeedback != null && commentFeedback.getType().equals(ContentType.COMMENT)) {
            commentOrFeedback = commentBuilder(commentFeedback);
        }
        result.setContent(commentOrFeedback);

        /**
         * 조회한 최근에 스크랩한 12개의 post들을 dto로 mapping.
         */
        List<ResPostDto> posts = postService.findRecentTwelveScrapedPost(myMember).stream()
                .map(post -> ResPostDto.builder()
                        .postId(post.getPostId())
                        .postMember(new ResMemberInfoDto(post.getMember().getMemberId(),
                                post.getMember().getNickname(),
                                post.getMember().getProfileImage(),
                                followService.isMyMemberFollowMember(myMember, post.getMember())))
                        .thumbnail(post.getThumbnailUrl())
                        .title(post.getTitle())
                        .likeCount(post.getLikeCount())
                        .isLikedPost((myMember != null && !post.getMember().getMemberId().equals(myMember.getMemberId())) ? postService.isThisPostLiked(myMember, post) : null)
                        .postingTime(post.getPostingTime().toString())
                        .views(post.getViews())
                        .scrapCount(post.getScrapCount())
                        .isScrapedPost((myMember != null && !post.getMember().getMemberId().equals(myMember.getMemberId())) ? postService.isThisPostScraped(myMember, post) : null)
                        .categoryName(post.getCategory().getName())
                        .build()).collect(Collectors.toList());
        result.setPosts(posts);

        BaseResponse<ResMyActivityDto> response = new BaseResponse<>(CustomMessage.OK, result);
        return ResponseEntity.ok(response);
        //return successResponse(CustomMessage.OK, result);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 작성한 코멘트/피드백을 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/comment-feedback")
    @Operation(summary = "내가 작성한 코멘트/피드백 조회 Test Completed", description = "내가 작성한 최신 코멘트/피드백들을 조회합니다." +
            "<br> asc  : 오래된순" +
            "<br> desc  : 최신순(default)")
    public ResponseEntity<BaseResponse<ResMyCommentFeedbackPaging>> myCommentFeedback(@RequestParam(name = "page", defaultValue = "1", required = false) int pageNum,
                                                                                      @RequestParam(name = "sort", defaultValue = "desc", required = false) String sortDirection,
                                                                                      @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Get /user/comment-feedback");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);

        pageNum -= 1;
        if (pageNum < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        Map<String, Object> result = new HashMap<>();

        Page<CommentFeedback> commentOrFeedback = commentFeedbackService.getMyCommentOrFeedback(pageNum, myMember, sortDirection);

        //조회할 레퍼런스가 db에 있으나, 현재 페이지에 조회할 데이터가 없는 경우 == 페이지 번호를 잘못 입력
        if ((commentOrFeedback.getContent().isEmpty()) && (commentOrFeedback.getTotalElements() > 0)) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        /**
         * 조회한 가장 최근에 작성한 댓글들을 dto로 mapping
         * 하나의 게시물에 여러 코멘트, 피드백 모두 단 경우 하나의 최신 하나만 보여주도록 구현
         */

        List<ResCommentFeedbackDto> contents = commentOrFeedback.stream()
                .map(commentFeedback -> {
                    ResCommentFeedbackDto map = null;
                    if (commentFeedback.getType().equals(ContentType.FEEDBACK)) {
                        map = feedbackBuilder(commentFeedback);
                    } else if (commentFeedback.getType().equals(ContentType.COMMENT)) {
                        map = commentBuilder(commentFeedback);
                    }
                    return map;
                }).collect(Collectors.toList());

        ResMyCommentFeedbackPaging myCommentFeedbackPaging = ResMyCommentFeedbackPaging.builder()
                .contents(contents)
                .totalPages(commentOrFeedback.getTotalPages())
                .totalOfAllComments(commentOrFeedback.getTotalElements())
                .totalOfPageElements(commentOrFeedback.getNumberOfElements())
                .build();

        BaseResponse<ResMyCommentFeedbackPaging> response = new BaseResponse<>(CustomMessage.OK, myCommentFeedbackPaging);
        return ResponseEntity.ok(response);
        //  return successResponse(CustomMessage.OK, result);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 작성한 코멘트 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/comment")
    @Operation(summary = "내가 작성한 코멘트 조회 Test Completed", description = "내가 작성한 코멘트 조회합니다." +
            "<br> asc  : 오래된순" +
            "<br> desc  : 최신순(default)")
    public ResponseEntity<BaseResponse<ResMyCommentPaging>> myComment(@RequestParam(name = "page", defaultValue = "1", required = false) int pageNum,
                                                                      @RequestParam(name = "sort", defaultValue = "desc", required = false) String sortDirection,
                                                                      @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Get /user/comment");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);

        pageNum -= 1;
        if (pageNum < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        Map<String, Object> result = new HashMap<>();

        Page<Comment> comments = commentService.getMyComments(pageNum, myMember, sortDirection);

        //조회할 레퍼런스가 db에 있으나, 현재 페이지에 조회할 데이터가 없는 경우 == 페이지 번호를 잘못 입력
        if ((comments.getContent().isEmpty()) && (comments.getTotalElements() > 0)) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        /**
         * 조회한 가장 최근에 작성한 댓글들을 dto로 mapping
         * 하나의 게시물에 여러 코멘트 단 경우 하나의 정렬 asc, desc에 따라 가장 오래된 것 또는 최신의 것 하나만 보여주도록 구현
         */
        List<ResMyCommentDto> contents = comments.stream()
                .map(comment -> {
                    ResMyCommentDto resMyCommentDto = commentBuilder(comment);
                    return resMyCommentDto;
                }).collect(Collectors.toList());

        ResMyCommentPaging myCommentPaging = ResMyCommentPaging.builder()
                .contents(contents)
                .totalPages(comments.getTotalPages())
                .totalOfAllComments(comments.getTotalElements())
                .totalOfPageElements(comments.getNumberOfElements())
                .build();

        BaseResponse<ResMyCommentPaging> response = new BaseResponse<>(CustomMessage.OK, myCommentPaging);
        return ResponseEntity.ok(response);
        //  return successResponse(CustomMessage.OK, result);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 작성한 피드백 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/feedback")
    @Operation(summary = "내가 작성한 피드백 조회 Test Completed", description = "내가 작성한 피드백 조회합니다." +
            "<br> asc  : 오래된순" +
            "<br> desc  : 최신순(default)")
    public ResponseEntity<BaseResponse<ResMyFeedbackPaging>> myFeedback(@RequestParam(name = "page", defaultValue = "1", required = false) int pageNum,
                                                                        @RequestParam(name = "sort", defaultValue = "desc", required = false) String sortDirection,
                                                                        @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Get /user/feedback");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);

        pageNum -= 1;
        if (pageNum < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }


        Page<Feedback> feedbacks = feedbackService.getMyFeedback(pageNum, myMember, sortDirection);

        //조회할 레퍼런스가 db에 있으나, 현재 페이지에 조회할 데이터가 없는 경우 == 페이지 번호를 잘못 입력
        if ((feedbacks.getContent().isEmpty()) && (feedbacks.getTotalElements() > 0)) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
        }

        /**
         * 조회한 가장 최근에 작성한 피드백들을 dto로 mapping
         * 하나의 게시물에 여러 피드백 모두 단 경우 하나의 최신 하나만 보여주도록 구현
         */
        List<ResMyFeedbackDto> contents = feedbacks.stream()
                .map(this::feedbackBuilder).toList();

        ResMyFeedbackPaging myFeedbackPaging = ResMyFeedbackPaging.builder()
                .contents(contents)
                .totalPages(feedbacks.getTotalPages())
                .totalOfAllFeedbacks(feedbacks.getTotalElements())
                .totalOfPageElements(feedbacks.getNumberOfElements())
                .build();

        BaseResponse<ResMyFeedbackPaging> response = new BaseResponse<>(CustomMessage.OK, myFeedbackPaging);
        return ResponseEntity.ok(response);

    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 받은 코멘트/피드백을 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/receive")
    @Operation(summary = "내가 받은 코멘트/피드백 조회 Test Completed", description = "내가 받은 최신 코멘트/피드백들을 조회합니다." +
            " \"<br> category : \"idea\", \"marketing\", \"design\", \"video\", \"digital\", \"etc\"")
    public ResponseEntity<BaseResponse<ResReceivedCommentFeedbackDto>> receivedCommentFeedback(@RequestParam(required = false, defaultValue = "all") String category,
                                                                                               @RequestParam(required = false, defaultValue = "1", name = "page") int pageNum,
                                                                                               @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Get /user/receive");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);

        pageNum -= 1;
        if (pageNum < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //    return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }


        Page<CommentFeedback> commentOrFeedbacks = commentFeedbackService.findReceivedCommentOrFeedback(myMember, pageNum, category);

        List<ResCommentFeedbackDto> contents = commentOrFeedbacks
                .stream()
                .map(commentFeedback -> {
                    ResCommentFeedbackDto map = null;
                    if (commentFeedback.getType().equals(ContentType.FEEDBACK)) {
                        map = feedbackBuilder(commentFeedback);
                    } else if (commentFeedback.getType().equals(ContentType.COMMENT)) {
                        map = commentBuilder(commentFeedback);
                    }
                    return map;
                }).collect(Collectors.toList());


        ResReceivedCommentFeedbackDto responseDto = ResReceivedCommentFeedbackDto.builder()
                .contents(contents)
                .totalPages(commentOrFeedbacks.getTotalPages())
                .totalOfAllComments(commentOrFeedbacks.getTotalElements())
                .totalOfPageElements(commentOrFeedbacks.getNumberOfElements())
                .build();

        BaseResponse<ResReceivedCommentFeedbackDto> response = new BaseResponse<>(CustomMessage.OK, responseDto);
        return ResponseEntity.ok(response);
        // return successResponse(CustomMessage.OK, result);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 스크랩한 게시글을 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/scrap") // 내가 스크랩한 게시글 확인
    @Operation(summary = "내가 스크랩한 게시글 조회 Test Completed", description = "내가 스크랩한 게시글들을 확인합니다." +
            "<br> category : \"idea\", \"marketing\", \"design\", \"video\", \"digital\", \"etc\" " +
            "<br> asc  : 오래된순" +
            "<br> desc  : 최신순(default)")
    public ResponseEntity<BaseResponse<ResMyScrapDto>> myScrap(
            @RequestParam(required = false, defaultValue = "all") String category,
            @RequestParam(required = false, defaultValue = "desc") String sort,
            @RequestParam(name = "page", defaultValue = "1", required = false) int pageNum,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("EndPoint Get /user/scrap");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);


        ResMyScrapDto resMyScrapDto = new ResMyScrapDto();

        pageNum -= 1;
        if (pageNum < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        /**
         * 조회한 최근에 스크랩한 12개의 post들을 dto로 mapping.
         */
        Page<PostScrap> posts = postService.findScrapedPost(pageNum, myMember, category, sort);

        //조회할 레퍼런스가 db에 있으나, 현재 페이지에 조회할 데이터가 없는 경우 == 페이지 번호를 잘못 입력
        if ((posts.getContent().isEmpty()) && (posts.getTotalElements() > 0)) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        List<ResPostDto> postDtoList = posts.stream()
                .map(PostScrap::getPost)
                .toList()
                .stream()
                .map(post -> ResPostDto.builder()
                        .postId(post.getPostId())
                        .postMember(new ResMemberInfoDto(post.getMember().getMemberId(),
                                post.getMember().getNickname(),
                                post.getMember().getProfileImage(),
                                followService.isMyMemberFollowMember(myMember, post.getMember())))
                        .thumbnail(post.getThumbnailUrl())
                        .title(post.getTitle())
                        .likeCount(post.getLikeCount())
                        .isLikedPost((myMember != null && !post.getMember().getMemberId().equals(myMember.getMemberId())) ? postService.isThisPostLiked(myMember, post) : null)
                        .postingTime(post.getPostingTime().toString())
                        .views(post.getViews())
                        .scrapCount(post.getScrapCount())
                        .isScrapedPost((myMember != null && !post.getMember().getMemberId().equals(myMember.getMemberId())) ? postService.isThisPostScraped(myMember, post) : null)
                        .categoryName(post.getCategory().getName()).build())
                .collect(Collectors.toList());

        ResMyScrapDto myScrapDto = ResMyScrapDto.builder()
                .posts(postDtoList)
                .totalPages(posts.getTotalPages())  //전체 페이지의 수
                .totalOfAllPosts(posts.getTotalElements())//모든 게시글 수
                .totalOfPageElements(posts.getNumberOfElements())//현 페이지 게시글 수
                .build();

        BaseResponse<ResMyScrapDto> response = new BaseResponse<>(CustomMessage.OK, myScrapDto);
        return ResponseEntity.ok(response);
        //  return successResponse(CustomMessage.OK, result);
    }

    private ResMyCommentDto commentBuilder(Comment comment) {
        return ResMyCommentDto.builder()
                .title(comment.getPost().getTitle())
                .postId(comment.getPost().getPostId())
                .commentId(comment.getCommentId())
                .thumbnail(comment.getPost().getThumbnailUrl())
                .member(new ResMemberInfoDto(comment.getMember().getMemberId(),
                        comment.getMember().getNickname(),
                        comment.getMember().getProfileImage(),
                        null))
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .build();
    }

    private ResMyFeedbackDto feedbackBuilder(Feedback feedback) {
        return ResMyFeedbackDto.builder()
                .title(feedback.getPost().getTitle())
                .postId(feedback.getPost().getPostId())
                .feedbackId(feedback.getFeedbackId())
                .thumbnail(feedback.getPost().getThumbnailUrl())
                .member(new ResMemberInfoDto(feedback.getMember().getMemberId(),
                        feedback.getMember().getNickname(),
                        feedback.getMember().getProfileImage(),
                        null))
                .content(feedback.getContent())
                .likeCount(feedback.getLikeCount()).build();
    }


    private ResCommentFeedbackDto commentBuilder(CommentFeedback commentFeedback) {
        return ResCommentFeedbackDto.builder()
                .title(commentFeedback.getPost().getTitle())
                .postId(commentFeedback.getPost().getPostId())
                .commentId(commentFeedback.getComment().getCommentId())
                .thumbnail(commentFeedback.getPost().getThumbnailUrl())
                .member(new ResMemberInfoDto(commentFeedback.getMember().getMemberId(),
                        commentFeedback.getMember().getNickname(),
                        commentFeedback.getMember().getProfileImage(),
                        null))
                .content(commentFeedback.getComment().getContent())
                .likeCount(commentFeedback.getComment().getLikeCount())
                .isComment(true)
                .build();
    }

    private ResCommentFeedbackDto feedbackBuilder(CommentFeedback commentFeedback) {
        return ResCommentFeedbackDto.builder()
                .title(commentFeedback.getPost().getTitle())
                .postId(commentFeedback.getPost().getPostId())
                .feedbackId(commentFeedback.getFeedback().getFeedbackId())
                .thumbnail(commentFeedback.getPost().getThumbnailUrl())
                .member(new ResMemberInfoDto(commentFeedback.getMember().getMemberId(),
                        commentFeedback.getMember().getNickname(),
                        commentFeedback.getMember().getProfileImage(),
                        null))
                .content(commentFeedback.getFeedback().getContent())
                .likeCount(commentFeedback.getFeedback().getLikeCount())
                .isComment(commentService.existsByPost(commentFeedback.getPost()))
                .build();

    }


}
