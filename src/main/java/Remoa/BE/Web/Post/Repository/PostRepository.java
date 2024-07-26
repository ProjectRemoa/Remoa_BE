package Remoa.BE.Web.Post.Repository;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findByMember(Member member);

    @Query(value = "select * from post p where member_id = :memberId", nativeQuery = true)
    List<Post> findByMemberHard(@Param("memberId")Long memberId);

    @Modifying
    @Query(value = "delete from post p where member_id = :memberId", nativeQuery = true)
    void deletePostByMemberHard(@Param("memberId") Long memberId);



}
