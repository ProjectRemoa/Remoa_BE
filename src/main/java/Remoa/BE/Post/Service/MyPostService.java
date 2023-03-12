package Remoa.BE.Post.Service;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPostService {

    private final PostRepository postRepository;

    public List<Post> showOnesPosts(Member member, Integer pageNumber) {
        List<Post> allPosts = postRepository.findByMember(member);

        Collections.sort(allPosts, (post1, post2) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime1 = LocalDateTime.parse(post1.getPostingTime(), formatter);
            log.warn("date1 = {}", dateTime1);
            LocalDateTime dateTime2 = LocalDateTime.parse(post2.getPostingTime(), formatter);
            log.warn("date1 = {}", dateTime2);

            if (dateTime1.isAfter(dateTime2)) {
                return -1;
            } else if (dateTime1.isBefore(dateTime2)) {
                return 1;
            } else {
                return 0;
            }
        });

        allPosts.subList(pageNumber * 5, )

    }
}
