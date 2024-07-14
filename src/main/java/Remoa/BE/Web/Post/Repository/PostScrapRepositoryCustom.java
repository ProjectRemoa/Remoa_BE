package Remoa.BE.Web.Post.Repository;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Category;
import Remoa.BE.Web.Post.Domain.PostScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostScrapRepositoryCustom {


    Page<PostScrap> findMyScrapedPost(Member member, Pageable pageable, Category category, String sort);

}
