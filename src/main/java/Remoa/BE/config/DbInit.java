package Remoa.BE.config;

import Remoa.BE.Post.Domain.Category;
import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Post.Service.CategoryService;
import Remoa.BE.Member.Service.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbInit {
    private final SignupService signupService;
    private final CategoryService categoryService;

    /**
     * @PostConstruct로 Admin 계정을 생성해주는 메서드
     */
    @PostConstruct
    public void createAdminUser() {
        if (signupService.isAdminExist()) {
            //do nothing
            log.info("============Admin is already exist============");
        } else {
            Member adminMember = new Member();
            adminMember.setEmail("spparta@gmail.com");
            adminMember.setPassword("admin");
            adminMember.setName("admin");
            adminMember.setBirth("00000000");
            adminMember.setSex(true);
            adminMember.setPhoneNumber("01000000000");
            adminMember.setOneLineIntroduction("관리자입니다.");
            adminMember.setTermConsent(true);
            adminMember.setRole("ROLE_ADMIN");
            this.signupService.join(adminMember);
            log.info("============Add Admin user completely============");
        }
    }

    /**
     * @PostConstruct로 Category를 세팅해주는 메서드
     */
    @PostConstruct
    public void initCategories() {
        if (!categoryService.findAllCategories().isEmpty()) {
            //do nothing
            log.info("==========Categories are already set==========");
        } else {
            Category idea = new Category("idea"); //기획 아이디어
            Category marketing = new Category("marketing"); //광고 마케팅
            Category design = new Category("design"); //디자인, 사진
            Category video = new Category("video"); //영상
            Category etc = new Category("etc"); //기타 아이디어
            this.categoryService.persistCategory(idea, marketing, design, video, etc);
            log.info("==========Setting Categories completely==========");
        }
    }

}
