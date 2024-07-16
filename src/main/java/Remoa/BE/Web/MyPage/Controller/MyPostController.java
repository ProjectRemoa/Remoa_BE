package Remoa.BE.Web.MyPage.Controller;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Member.Dto.Res.ResMemberInfoDto;
import Remoa.BE.Web.Member.Service.FollowService;
import Remoa.BE.Web.Member.Service.MemberService;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Dto.Response.PostPageResponseDto;
import Remoa.BE.Web.Post.Dto.Response.ResPostDto;
import Remoa.BE.Web.MyPage.Service.MyPostService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static Remoa.BE.config.DbInit.categoryList;

@Tag(name = "나의 레퍼런스 조회 기능 Test Completed", description = "나의 레퍼런스 조회 기능 API")
@Slf4j
@RestController
@RequiredArgsConstructor
public class MyPostController {

    private final MemberService memberService;
    private final PostService postService;
    private final MyPostService myPostService;
    private final FollowService followService;

    // Entity <-> DTO 간의 변환을 편리하게 하고자 ModelMapper 사용.(build.gradle에 의존성 주입 완료)
//    private final ModelMapper modelMapper = new ModelMapper();


    /**
     * 내 작업물 목록 페이지
     */

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 레퍼런스를 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = MessageUtils.UNAUTHORIZED,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/reference")
    @Operation(summary = "내 레퍼런스 목록 조회 Test Completed", description = "내가 작성한 레퍼런스 목록을 조회합니다. " +
            "<br> category : \"idea\", \"marketing\", \"design\", \"video\", \"digital\", \"etc\"" +
            "<br> sort : \"views\", \"likes\", \"scrap\"")
    public ResponseEntity<BaseResponse<PostPageResponseDto>> myReference(HttpServletRequest request,
                                                                         @RequestParam(required = false, defaultValue = "all") String category,
                                                                         @RequestParam(required = false, defaultValue = "1", name = "page") int pageNumber,
                                                                         @RequestParam(required = false, defaultValue = "newest") String sort,
                                                                         @RequestParam(required = false, defaultValue = "") String title,
                                                                         @AuthenticationPrincipal MemberDetails memberDetails) {

        log.info("EndPoint Get /user/reference");

        Long memberId = memberDetails.getMemberId();
        Member myMember = memberService.findOne(memberId);

        pageNumber -= 1;
        if (pageNumber < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            // return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        Page<Post> posts;

        if (categoryList.contains(category)) {
            posts = myPostService.sortAndPaginatePostsByCategoryAndMember(category, pageNumber, sort, myMember, title);
        } else {
            posts = myPostService.sortAndPaginatePostsByMember(pageNumber, sort, myMember, title);
        }

        //조회할 레퍼런스가 db에 있으나, 현재 페이지에 조회할 데이터가 없는 경우 == 페이지 번호를 잘못 입력
        if ((posts.getContent().isEmpty()) && (posts.getTotalElements() > 0)) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            // return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        List<ResPostDto> result = new ArrayList<>();

        for (Post post : posts) {
            ResPostDto map = ResPostDto.builder()
                    .postingTime(post.getPostingTime().toString())
                    .postMember(new ResMemberInfoDto(post.getMember().getMemberId(),
                            post.getMember().getNickname(),
                            post.getMember().getProfileImage(),
                            null))
                    .postId(post.getPostId())
                    .views(post.getViews())
                    .categoryName(post.getCategory().getName())
                    .likeCount(post.getLikeCount())
                    .thumbnail(post.getThumbnailUrl())
                    .scrapCount(post.getScrapCount())
                    .title(post.getTitle()).build();
            result.add(map);
        }
        //프론트에서 쓰일 조회한 레퍼런스들과 페이지 관련한 값들 map에 담아서 return.

        PostPageResponseDto responseDto = PostPageResponseDto.builder()
                .references(result) //조회한 레퍼런스들
                .totalPages(posts.getTotalPages()) //전체 페이지의 수
                .totalOfAllReferences(posts.getTotalElements()) //모든 레퍼런스의 수
                .totalOfPageElements(posts.getNumberOfElements()) //현 페이지의 레퍼런스 수
                .build();


        BaseResponse<PostPageResponseDto> response = new BaseResponse<>(CustomMessage.OK, responseDto);
        return ResponseEntity.ok(response);
        // return successResponse(CustomMessage.OK, referencesAndPageInfo);

    }


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "다른 사용자의 레퍼런스를 성공적으로 조회했습니다."),
            @ApiResponse(responseCode = "400", description = "페이지 번호가 잘못되었습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/reference/{member_id}")
    @Operation(summary = "다른 사용자의 레퍼런스 목록 조회 Test Completed", description = "다른 사용자가 작성한 레퍼런스 목록을 조회합니다." +
            "<br> category : \"idea\", \"marketing\", \"design\", \"video\", \"digital\", \"etc\"" +
            "<br> sort : \"views\", \"likes\", \"scrap\"")
    public ResponseEntity<BaseResponse<PostPageResponseDto>> otherReference(HttpServletRequest request,
                                                 @PathVariable("member_id") Long memberId,
                                                 @RequestParam(required = false, defaultValue = "all") String category,
                                                 @RequestParam(required = false, defaultValue = "1", name = "page") int pageNumber,
                                                 @RequestParam(required = false, defaultValue = "newest") String sort,
                                                 @RequestParam(required = false, defaultValue = "") String title,
                                                 @AuthenticationPrincipal MemberDetails memberDetails) {
        log.info("EndPoint Get /user/reference/{member_id}");

        Member myMember = null;
        if (memberDetails != null) {
            Long myMemberId = memberDetails.getMemberId();
            myMember = memberService.findOne(myMemberId);
        }


        Member selectedMember = memberService.findOne(memberId);

        pageNumber -= 1;
        if (pageNumber < 0) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        Page<Post> posts;

        if (categoryList.contains(category)) {
            posts = myPostService.sortAndPaginatePostsByCategoryAndMember(category, pageNumber, sort, selectedMember, title);
        } else {
            posts = myPostService.sortAndPaginatePostsByMember(pageNumber, sort, selectedMember, title);
        }

        //조회할 레퍼런스가 db에 있으나, 현재 페이지에 조회할 데이터가 없는 경우 == 페이지 번호를 잘못 입력
        if ((posts.getContent().isEmpty()) && (posts.getTotalElements() > 0)) {
            throw new BaseException(CustomMessage.PAGE_NUM_OVER);
            //  return errorResponse(CustomMessage.PAGE_NUM_OVER);
        }

        List<ResPostDto> result = new ArrayList<>();

        for (Post post : posts) {
            ResPostDto map = ResPostDto.builder()
                    .postingTime(post.getPostingTime().toString())
                    .postMember(new ResMemberInfoDto(post.getMember().getMemberId(),
                            post.getMember().getNickname(),
                            post.getMember().getProfileImage(),
                            myMember != null ? followService.isMyMemberFollowMember(myMember, post.getMember()) : null))
                    .postId(post.getPostId())
                    .views(post.getViews())
                    .categoryName(post.getCategory().getName())
                    .likeCount(post.getLikeCount())
                    .isLikedPost((myMember != null && !post.getMember().getMemberId().equals(myMember.getMemberId())) ? postService.isThisPostLiked(myMember, post) : null)
                    .thumbnail(post.getThumbnailUrl())
                    .scrapCount(post.getScrapCount())
                    .isScrapedPost((myMember != null && !post.getMember().getMemberId().equals(myMember.getMemberId())) ? postService.isThisPostScraped(myMember, post) : null)
                    .title(post.getTitle()).build();
            result.add(map);
        }
        //프론트에서 쓰일 조회한 레퍼런스들과 페이지 관련한 값들 map에 담아서 return.
        PostPageResponseDto responseDto = PostPageResponseDto.builder()
                .references(result) //조회한 레퍼런스들
                .totalPages(posts.getTotalPages()) //전체 페이지의 수
                .totalOfAllReferences(posts.getTotalElements()) //모든 레퍼런스의 수
                .totalOfPageElements(posts.getNumberOfElements()) //현 페이지의 레퍼런스 수
                .build();
        BaseResponse<PostPageResponseDto> response = new BaseResponse<>(CustomMessage.OK, responseDto);
        return ResponseEntity.ok(response);
        //  return successResponse(CustomMessage.OK, referencesAndPageInfo);
    }


}
