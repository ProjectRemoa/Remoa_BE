package Remoa.BE.Web.Post.Controller;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Res.ResMemberInfoDto;
import Remoa.BE.Web.Member.Service.FollowService;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Domain.UploadFile;
import Remoa.BE.Web.Post.Dto.Request.UploadPostForm;
import Remoa.BE.Web.MyPage.Service.MyPostService;
import Remoa.BE.Web.Post.Service.PostService;
import Remoa.BE.Web.Post.Dto.Response.*;
import Remoa.BE.config.auth.MemberDetails;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import Remoa.BE.exception.response.BaseResponse;
import Remoa.BE.exception.response.ErrorResponse;
import Remoa.BE.utill.CommonFunction;
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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static Remoa.BE.config.DbInit.categoryList;

@Tag(name = "레퍼런스 기능 Test Completed", description = "레퍼런스 기능 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MyPostService myPostService;
    private final MemberService memberService;
    private final FollowService followService;
    private final ModelMapper modelMapper;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레퍼런스를 성공적으로 검색했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/reference")
    @Operation(summary = "레퍼런스 검색 Test completed", description = "레퍼런스를 검색합니다." +
            "<br> category : \"idea\", \"marketing\", \"design\", \"video\", \"digital\", \"etc\"" +
            "<br> sort : \"views\", \"likes\", \"scrap\", \"newest(default)\" " )
    public ResponseEntity<BaseResponse<SearchPostResponseDto>> searchPost(HttpServletRequest request,
                                                                          @RequestParam(required = false, defaultValue = "all") String category,
                                                                          @RequestParam(required = false, defaultValue = "newest") String sort,
                                                                          @RequestParam(required = false, defaultValue = "1", name = "page") int pageNumber,
                                                                          @RequestParam(required = false, defaultValue = "") String searchQuery,
                                                                          @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        log.info("EndPoint Get /reference");
        Member myMember = null; //로그인 한 경우 좋아요, 스크랩 표시하기 위한 분기. -> 토큰 방식으로 바뀌어 수정 필요.
        if (memberDetails != null) {
            Long myMemberId = memberDetails.getMemberId();
            myMember = memberService.findOne(myMemberId);
        }

        Map<String, Object> responseData = new HashMap<>();

        pageNumber -= 1;
        if (pageNumber < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            // return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        Page<Post> allPosts;
        if (categoryList.contains(category)) {
            //sort -> 최신순 : newest, 좋아요순 : like, 스크랩순 : scrap, 조회순 : view
            allPosts = postService.sortAndPaginatePostsByCategory(category, sort, pageNumber, searchQuery);
        } else {
            //sort -> 최신순 : newest, 좋아요순 : like, 스크랩순 : scrap, 조회순 : view
            allPosts = postService.sortAndPaginatePosts(sort, pageNumber, searchQuery);
        }

        if ((allPosts.getContent().isEmpty()) && (allPosts.getTotalElements() > 0)) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            // return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        List<ResHomeReferenceDto> result = new ArrayList<>();

        for (Post post : allPosts) {
            ResHomeReferenceDto map = ResHomeReferenceDto.builder()
                    .postThumbnail(post.getThumbnailUrl())
                    .postId(post.getPostId())
                    .title(post.getTitle())
                    .views(post.getViews())
                    .likeCount(post.getLikeCount())
                    .isLikedPost(isLikedPost(myMember, post))
                    .scrapCount(post.getScrapCount())
                    .isScrapedPost(isScrapedPost(myMember, post))
                    .postMember(new ResMemberInfoDto(post.getMember().getMemberId(),
                            post.getMember().getNickname(),
                            post.getMember().getProfileImage(),
                            myMember != null ? followService.isMyMemberFollowMember(myMember, post.getMember()) : null))
                    .build();

            result.add(map);
        }

        SearchPostResponseDto responseDto = SearchPostResponseDto.builder()
                .references(result) //조회한 레퍼런스들
                .totalPages(allPosts.getTotalPages()) //전체 페이지의 수
                .totalOfAllReferences(allPosts.getTotalElements()) //모든 레퍼런스의 수
                .totalOfPageElements(allPosts.getNumberOfElements()) //현 페이지의 레퍼런스 수
                .build();

        BaseResponse<SearchPostResponseDto> response = new BaseResponse<>(CustomMessage.OK, responseDto);
        return ResponseEntity.ok(response);
        //   return successResponse(CustomMessage.OK, responseData);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레퍼런스 등록 성공"),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/reference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //게시물 등록
    @Operation(summary = "레퍼런스 등록 Test completed", description = "레퍼런스를 등록합니다.")
    public ResponseEntity<BaseResponse<ResReferenceRegisterDto>> share(@RequestPart("data") UploadPostForm uploadPostForm,
                                                                       @RequestPart("thumbnail") MultipartFile thumbnail,
                                                                       @RequestPart(value = "file", required = false) List<MultipartFile> uploadFiles,
                                                                       @AuthenticationPrincipal MemberDetails memberDetails) throws IOException {
        log.info("EndPoint Post /reference");

        Long memberId = memberDetails.getMemberId();
        Post savePost = postService.registerPost(uploadPostForm, thumbnail, uploadFiles, memberId);
        Post post = postService.findOne(savePost.getPostId());

        ResReferenceRegisterDto resReferenceRegisterDto = ResReferenceRegisterDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .category(post.getCategory().getName())
                .contestAwardType(post.getContestAwardType())
                .contestName(post.getContestName())
                .youtubeLink(post.getYoutubeLink())
                .pageCount(post.getPageCount())
                .build();
        if (post.getUploadFiles() != null) {
            resReferenceRegisterDto.setFileNames(post.getUploadFiles().stream()
                    .map(UploadFile::getOriginalFileName)
                    .collect(Collectors.toList()));
        }
        BaseResponse<ResReferenceRegisterDto> response = new BaseResponse<>(CustomMessage.OK, resReferenceRegisterDto);
        return ResponseEntity.ok(response);
        //return successResponse(CustomMessage.OK, resReferenceRegisterDto);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레퍼런스 수정 성공"),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping(value = "/reference/{reference_id}" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE) // 게시물 수정
    @Operation(summary = "레퍼런스 수정 Test completed", description = "레퍼런스를 수정합니다.")
    public ResponseEntity<BaseResponse<ResReferenceRegisterDto>> modify(@PathVariable("reference_id") Long referenceId,
                                                                        @RequestPart("data") UploadPostForm uploadPostForm,
                                                                        @RequestPart("thumbnail") MultipartFile thumbnail,
                                                                        @RequestPart(value = "file", required = false) List<MultipartFile> uploadFiles,
                                                                        @AuthenticationPrincipal MemberDetails memberDetails) throws IOException {
        log.info("EndPoint Put /reference/{reference_id}");

        Long memberId = memberDetails.getMemberId();
        Post post = postService.findOne(referenceId);
        if (!Objects.equals(memberId, post.getMember().getMemberId())) {
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
            //return errorResponse(CustomMessage.CAN_NOT_ACCESS);
        }
        Post modifiedPost = postService.modifyPost(uploadPostForm, thumbnail, uploadFiles, post);


        ResReferenceRegisterDto resReferenceRegisterDto = ResReferenceRegisterDto.builder()
                .postId(modifiedPost.getPostId())
                .title(modifiedPost.getTitle())
                .category(modifiedPost.getCategory().getName())
                .contestAwardType(modifiedPost.getContestAwardType())
                .contestName(modifiedPost.getContestName())
                .youtubeLink(modifiedPost.getYoutubeLink())
                .pageCount(modifiedPost.getPageCount())
                .build();
        if (modifiedPost.getUploadFiles() != null) {
            resReferenceRegisterDto.setFileNames(modifiedPost.getUploadFiles().stream()
                    .map(UploadFile::getOriginalFileName)
                    .collect(Collectors.toList()));
        }
        BaseResponse<ResReferenceRegisterDto> response = new BaseResponse<>(CustomMessage.OK, resReferenceRegisterDto);
        return ResponseEntity.ok(response);
        //return successResponse(CustomMessage.OK, resReferenceRegisterDto);

    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레퍼런스 좋아요 성공"),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reference/{reference_id}/like")
    @Operation(summary = "레퍼런스 좋아요 Test completed", description = "레퍼런스에 좋아요를 합니다.")
    public ResponseEntity<BaseResponse<LikePostResponseDto>> likeReference(@PathVariable("reference_id") Long referenceId,
                                                                           @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Post /reference/{reference_id}/like");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);
        Member postedMember = postService.getPostedMember(referenceId);
        if (myMember.equals(postedMember)) {
            throw new BaseException(CustomMessage.SELF_LIKE);
            //return errorResponse(CustomMessage.SELF_LIKE);
        }
        Post post = postService.likePost(memberId, myMember, referenceId);
        LikePostResponseDto responseDto = LikePostResponseDto.builder()
                .likeCount(post.getLikeCount())
                .build();

        BaseResponse<LikePostResponseDto> response = new BaseResponse<>(CustomMessage.OK, responseDto);
        return ResponseEntity.ok(response);
        //return successResponse(CustomMessage.OK, map);

    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레퍼런스 스크랩 성공"),
            @ApiResponse(responseCode = "201", description = "레퍼런스 스크랩 해제"),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reference/{reference_id}/scrap")
    @Operation(summary = "레퍼런스 스크랩 Test completed", description = "레퍼런스를 스크랩합니다.")
    public ResponseEntity<BaseResponse<ScrapReferenceResponseDto>> scrapReference(@PathVariable("reference_id") Long referenceId,
                                                                                  @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Post /reference/{reference_id}/scrap");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);
        Member postedMember = postService.getPostedMember(referenceId);
        if (myMember.equals(postedMember)) {
            throw new BaseException(CustomMessage.SELF_SCRAP);
            //return errorResponse(CustomMessage.SELF_SCRAP);
        }
        boolean isScrapAction = postService.scrapPost(memberId, myMember, referenceId);
        Post post = postService.findOne(referenceId);
        ScrapReferenceResponseDto responseDto = ScrapReferenceResponseDto.builder()
                .scrapCount(post.getScrapCount())
                .build();
        // 스크랩의 경우 : 200 OK, 스크랩 해제의 경우 : 201 CREATED
        CustomMessage customMessage = isScrapAction ? CustomMessage.OK_SCRAP : CustomMessage.OK_UNSCRAP;
        BaseResponse<ScrapReferenceResponseDto> response = new BaseResponse<>(customMessage, responseDto);
        return ResponseEntity.ok(response);
        //return successResponse(customMessage, map);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레퍼런스 삭제 성공"),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/user/reference/{reference_id}")
    @Operation(summary = "레퍼런스 삭제 Test completed", description = "레퍼런스를 삭제합니다.")
    public ResponseEntity<Void> deleteReference(@PathVariable("reference_id") Long[] postId,
                                                  @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Delete /user/reference/{reference_id}");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);

        // 현재 로그인한 사용자가 올린 게시글이 맞는지 확인/예외처리
        for (int i = 0; i < postId.length; i++) {
            if (postService.checkMemberPost(myMember, postId[i])) {
                postService.deleteReference(postId[i]);
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "레퍼런스 삭제 성공"),
            @ApiResponse(responseCode = "400", description = MessageUtils.BAD_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/user/referenceCategory/{category}")
    @Operation(summary = "레퍼런스 카테고리 삭제", description = "레퍼런스 카테고리를 삭제합니다.")
    public ResponseEntity<Void> deleteReferenceCategory(@PathVariable("category") String category,
                                                          @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Delete /user/referenceCategory/{category}");

        Long categoryId = CommonFunction.getCategoryId(category); // 카테고리 id 추출.
        Long memberId = memberDetails.getMemberId();
        myPostService.deleteReferenceCategory(memberId, categoryId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Boolean isLikedPost(Member myMember, Post post) {
        return myMember != null && postService.isThisPostLiked(myMember, post);
    }

    private Boolean isScrapedPost(Member myMember, Post post) {
        return myMember != null && postService.isThisPostScraped(myMember, post);
    }


}