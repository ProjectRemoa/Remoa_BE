package Remoa.BE.Post.Controller;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Post.Dto.Response.ResRegistCommentDto;
import Remoa.BE.Post.Service.CommentService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static Remoa.BE.exception.CustomBody.errorResponse;
import static Remoa.BE.exception.CustomBody.successResponse;
import static Remoa.BE.utill.MemberInfo.authorized;
import static Remoa.BE.utill.MemberInfo.getMemberId;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyPostController {

    private final MemberService memberService;
    private final CommentService commentService;
    @PostMapping("/reference/{reference_id}/comment")
    public ResponseEntity<Object> registComment(@RequestParam Map<String, String> comment, @PathVariable("reference_id") Long postId, HttpServletRequest request){
        String myComment = comment.get("comment");
        if(authorized(request)){
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);
            ResRegistCommentDto resRegistCommentDto = commentService.registComment(myMember, myComment, postId);
            return successResponse(CustomMessage.OK, resRegistCommentDto);
        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }
}