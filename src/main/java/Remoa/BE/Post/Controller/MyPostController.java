package Remoa.BE.Post.Controller;

import Remoa.BE.Post.Service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MyPostController {

    private final PostService postService;

 /*   @GetMapping("/user/reference")
    public ResponseEntity<Object> viewMyReference() {

    }*/
}
