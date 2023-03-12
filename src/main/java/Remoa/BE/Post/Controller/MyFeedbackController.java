package Remoa.BE.Post.Controller;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.UploadFile;
import Remoa.BE.Post.Dto.Response.ThumbnailFeedbackDto;
import Remoa.BE.Post.Service.MyPostService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static Remoa.BE.exception.CustomBody.errorResponse;
import static Remoa.BE.exception.CustomBody.successResponse;
import static Remoa.BE.utill.MemberInfo.authorized;
import static Remoa.BE.utill.MemberInfo.getMemberId;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyFeedbackController {

    private final ModelMapper modelMapper = new ModelMapper();
    private final MemberService memberService;
    private final MyPostService myPostService;

    @GetMapping("/user/feedback")
    public ResponseEntity<Object> myReceivedFeedback(HttpServletRequest request) {
        if (authorized(request)) {
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);

            List<Post> allPosts = myPostService.showOnesPosts(myMember);
            List<ThumbnailFeedbackDto> myReferenceList = new ArrayList<>();

            // ModelMapper initialize
            initModelMapper();

            for (Post post : allPosts) {
                // ModelMapper 통해 Entity -> DTO 변환
                ThumbnailFeedbackDto postDTO = modelMapper.map(post, ThumbnailFeedbackDto.class);

                myReferenceList.add(postDTO);
            }
            return successResponse(CustomMessage.OK, myReferenceList);
        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }

    /**
     * Post Entity -> Thumbnail용 Response DTO를 위한 ModelMapper.
     */
    private void initModelMapper() {
        /*modelMapper.typeMap(Post.class, ThumbnailFeedbackDto.class)
                .addMappings(mapper -> mapper.using(
                                (Converter<Member, String>) context -> context.getSource().getNickname())
                        .map(Post::getMember, ThumbnailFeedbackDto::setNickname))
                .addMappings(mapper -> mapper.using(
                                (Converter<List<UploadFile>, List<String>>) context -> context.getSource().stream()
                                        .map(UploadFile::getStoreFileUrl).collect(Collectors.toList()))
                        .map(Post::getUploadFiles, ThumbnailFeedbackDto::setStoreFileUrls));*/
    }
}
