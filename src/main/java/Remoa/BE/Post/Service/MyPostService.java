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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPostService {

    private final PostRepository postRepository;

    public List<Post> showOnesPosts(Member member, Integer pageNumber) {
        List<Post> allPosts = postRepository.findByMember(member);

        int postQuantity = allPosts.size();
        int remainder = postQuantity % 5;
        int totalPages = (remainder == 0) ? (postQuantity / 5) : ((postQuantity / 5) + 1);

        if (pageNumber > totalPages) {
            throw new IllegalArgumentException("페이지가 올바르지 않습니다.");
        }

        allPosts.sort((post1, post2) -> {
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

        int index = (pageNumber * 4) + pageNumber;

        if (pageNumber == totalPages) {
            return allPosts.subList(index - 5, postQuantity);
        }
        return allPosts.subList(index - 5, index);
    }
    //TODO 23.03.14 : branch 옮기기 -> 더 나은 방식의 paging 알고리즘 찾아보고 적용, 받은 피드백 목록 구현
}
