package Remoa.BE.Web.Post.Repository;

import Remoa.BE.Web.Comment.Domain.Comment;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Category;
import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Domain.PostLike;
import Remoa.BE.Web.Post.Domain.PostScrap;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {
    void savePost(Post post);

    void modifyPost(Post post);

    List<Post> findAll();

    Optional<Post> findOne(Long postId);

    List<Post> findByMember(Member member);

    List<Post> findByTitleContaining(String name);

    void savePostScrap(PostScrap postScrap);

    Optional<PostScrap> findScrapedPost(Member member, Post post);

    Optional<PostLike> findLikedPost(Member myMember, Post post);

    List<Post> findPostsByCategory(Category category);

    void saveComment(Comment comment);

    void deletePost(Long postId);

    Optional<Member> findPostedMember(Long postId);
}
