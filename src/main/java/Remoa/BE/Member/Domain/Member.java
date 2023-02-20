package Remoa.BE.Member.Domain;

import Remoa.BE.Post.Domain.Post;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Where(clause = "deleted = false")
public class Member implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long memberId;

    /**
     * 카카오 api에서 받아오는 고유 id값
     */
    @Column(name = "kakao_id")
    private Long kakaoId;

    /**
     * 카카오 api에서 받아오는 카카오 이메일
     */
    private String email;

    /**
     * 23.02.04 카카오 로그인 단독 개발로 인해 삭제 예정.
     */
    private String password;

    /**
     * 사용자 이름
     */
    private String name;

    /**
     * 카카오 api에서 받아오는 nickname 값
     */
    //23.1.19 추가
    private String nickname;

    private String birth;

    private Boolean sex;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String university;

    /**
     * 한 줄 소개
     */
    @Column(name = "one_line_introduction")
    private String oneLineIntroduction;

    /**
     * 레모아 서비스 이용 약관 동의 여부
     */
    @Column(name = "term_consent")
    private Boolean termConsent;

    /**
     * 카카오 api에서 받아오는 카카오 프로필 사진 uri
     */
    @Column(name = "profile_image")
    private String profileImage;

    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = {CascadeType.ALL})
    private List<MemberCategory> memberCategories = new ArrayList();

    @OneToMany(mappedBy = "member", cascade = {CascadeType.ALL})
    private List<CommentBookmark> commentBookmarks = new ArrayList();

    @OneToMany(mappedBy = "member", cascade = {CascadeType.ALL})
    private List<CommentLike> commentLikes = new ArrayList();

    @OneToMany(mappedBy = "fromMember")
    private List<Follow> follows = new ArrayList();

    /**
     * ADMIN과 일반 USER를 구분하기 위해 존재. Spring Security 이용하기 위함
     */
    private String role;

    private Boolean deleted = Boolean.FALSE;

    /**
     * SecureConfig를 통해 Bean에 등록된 passwordEncoder를 이용해 회원가입시 패스워드 암호화.
     * 현재(23.02.13 기준) 카카오 로그인으로 통합되어 kakaoId를 암호화 하는 방향으로 쓰이거나 사용하지 않을 예정
     * @param passwordEncoder
     * @return
     */
    public Member hashPassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
        return this;
    }

    //이 밑으로는 Spring Security를 사용해 로그인하기 위해 UserDetails를 구현하며 생긴 사용하지 않는 메서드들입니다.
    public Boolean checkPassword(String plainPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(plainPassword, this.password);
    }

    public void addMemberCategory(MemberCategory memberCategory) {
        memberCategories.add(memberCategory);
    }

    public void addCommentBookmark(MemberCategory memberCategory) {
        memberCategories.add(memberCategory);
    }

    public void addCommentLike(MemberCategory memberCategory) {
        memberCategories.add(memberCategory);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        for(String role : role.split(",")){
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    public String getUsername() {
        return null;
    }

    public boolean isAccountNonExpired() {
        return false;
    }

    public boolean isAccountNonLocked() {
        return false;
    }

    public boolean isCredentialsNonExpired() {
        return false;
    }

    public boolean isEnabled() {
        return false;
    }

}
