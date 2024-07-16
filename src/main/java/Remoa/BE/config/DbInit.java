package Remoa.BE.config;

import Remoa.BE.Web.Member.Dto.GerneralLoginDto.AdminSignUpReq;
import Remoa.BE.Web.Member.Dto.GerneralLoginDto.GeneralSignUpReq;
import Remoa.BE.Web.Member.Dto.GerneralLoginDto.GeneralSignUpRes;
import Remoa.BE.Web.Post.Domain.Category;
import Remoa.BE.Web.Post.Service.CategoryService;
import Remoa.BE.Web.Member.Service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.List;




@Slf4j
@Component
@RequiredArgsConstructor
public class DbInit {
    private final CategoryService categoryService;
    private final MemberService memberService;

    public static final List<String> categoryList = List.of("idea", "marketing", "design", "video", "digital", "etc");

    /**
     * @PostConstruct로 Category를 세팅해주는 메서드
     */
    @PostConstruct
    public void initCategories() {
        if (!categoryService.findAllCategories().isEmpty()) {
            //do nothing
            log.info("==========Categories are already set==========");
        } else {
            Category idea = new Category(categoryList.get(0));
            Category marketing = new Category(categoryList.get(1));
            Category design = new Category(categoryList.get(2));
            Category video = new Category(categoryList.get(3));
            Category digital = new Category(categoryList.get(4));
            Category etc = new Category(categoryList.get(5));
            this.categoryService.persistCategory(idea, marketing, design, video, digital, etc);
            log.info("==========Setting Categories completely==========");
        }
    }

    @PostConstruct
    public void initAdmin() {
        AdminSignUpReq adminSignUpReq = AdminSignUpReq.builder()
                .account("referencemoa")
                .password("fpahdk2023!")
                .name("관리자")
                .build();

        memberService.adminSignUp(adminSignUpReq);
    }


    @PostConstruct
    public void initMembers() {

        GeneralSignUpReq signUpReq1 = GeneralSignUpReq.builder()
                .account("test1@gmail.com")
                .password("testPassword1")
                .name("김김김")
                .build();

        GeneralSignUpReq signUpReq2 = GeneralSignUpReq.builder()
                .account("test2@gmail.com")
                .password("testPassword2")
                .name("이이이")
                .build();

        GeneralSignUpReq signUpReq3 = GeneralSignUpReq.builder()
                .account("test3@gmail.com")
                .password("testPassword3")
                .name("박박박")
                .build();

        GeneralSignUpReq signUpReq4 = GeneralSignUpReq.builder()
                .account("test4@gmail.com")
                .password("testPassword4")
                .name("최최최")
                .build();


        memberService.generalSignUp(signUpReq1);
        memberService.generalSignUp(signUpReq2);
        memberService.generalSignUp(signUpReq3);
        memberService.generalSignUp(signUpReq4);
    }

}
