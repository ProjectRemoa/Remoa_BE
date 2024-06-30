package Remoa.BE.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomMessage {
    //200 정상처리 상태코드
    OK(HttpStatus.OK, "올바른 요청", "정상적으로 처리되었습니다"),

    OK_DUPLICATE(HttpStatus.OK, "올바른 요청", "닉네임이 중복되었습니다"),

    OK_UN_DUPLICATE(HttpStatus.OK, "올바른 요청", "사용가능한 닉네임 입니다"),

    OK_UNFOLLOW(HttpStatus.OK, "올바른 요청", "회원을 언팔로잉 합니다"),

    OK_SCRAP(HttpStatus.OK, "올바른 요청", "정상적으로 스크랩 되었습니다."),

    //201 한 api에서 정상처리상 구분이 필요할떄 사용
    OK_FOLLOW(HttpStatus.CREATED, "올바른 요청", "회원을 팔로잉 합니다"),

    OK_UNSCRAP(HttpStatus.CREATED, "올바른 요청", "정상적으로 스크랩이 해제됐습니다."),

    OK_SIGNUP(HttpStatus.CREATED, "올바른 요청", "회원가입하는 회원입니다"),

    //400 잘못된 요청
    VALIDATED(HttpStatus.BAD_REQUEST, "잘못된 요청", "요청한 값이 유효성검사를 통과하지 못했습니다"),
    NO_ID(HttpStatus.BAD_REQUEST, "잘못된 요청", "요청한 Id가 존재하지 않습니다"),
    NO_CATEGORY(HttpStatus.BAD_REQUEST, "잘못된 요청", "요청한 카테고리가 존재하지 않습니다"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청", "잘 못된 요청입니다"),

    SELF_FOLLOW(HttpStatus.BAD_REQUEST, "서비스 로직상 오류", "자신을 팔로우할 수 없습니다"),

    SELF_LIKE(HttpStatus.BAD_REQUEST, "서비스 로직상 오류", "자신의 게시물을 좋아요할 수 없습니다"),

    SELF_SCRAP(HttpStatus.BAD_REQUEST, "서비스 로직상 오류", "자신을 게시물을 스크랩할 수 없습니다"),

    BAD_DUPLICATE(HttpStatus.BAD_REQUEST, "서비스 로직상 오류", "닉네임이 중복되었습니다"),

    BAD_PROFILE_IMG(HttpStatus.BAD_REQUEST, "서비스 로직상 오류", "해당 멤버의 프로필사진이 존재하지 않습니다"),

    BAD_FILE(HttpStatus.BAD_REQUEST, "서비스 로직상 오류", "해당 파일은 지원하지 않습니다."),

    BAD_PAGE_NUM(HttpStatus.BAD_REQUEST, "피드백 등록시 페이지 넘버 오류", "존재하지 않는 페이지에 피드백을 등록하려 합니다"),

    PAGE_NUM_OVER(HttpStatus.BAD_REQUEST, "레퍼런스 조회시 페이지 넘버 오류", "올바르지 않는 페이지 번호입니다."),

    FILE_SIZE_OVER(HttpStatus.BAD_REQUEST, "파일 업/다운로드 오류", "파일 사이즈가 최대 허용 크기보다 큽니다."),
    IMAGE_PIXEL_LACK(HttpStatus.BAD_REQUEST, "파일 업/다운로드 오류", "이미지 픽셀이 최소 규격에 미달합니다."),
    INVALID_FILE_LENGTH(HttpStatus.BAD_REQUEST, "파일 길이 초과", "업로드하려는 파일의 길이가 너무 깁니다."),
    CANNOT_REISSUE_TOKEN(HttpStatus.BAD_REQUEST, "토큰 재발급 실패", "토큰 재발급 실패하였습니다"),


    //401권한오류
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "권한이 없습니다", "인증에 필요한 쿠키 정보가 없습니다"),
    NOT_VALID_TOKEN(HttpStatus.UNAUTHORIZED, "권한이 없습니다", "토큰이 유효하지 않습니다."),
    NO_TOKEN_FOUND(HttpStatus.UNAUTHORIZED, "권한이 없습니다", "토큰이 없습니다."),
    EXPIRED_TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED, "권한이 없습니다", "만료 토큰이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.LOCKED, "엑세스 토큰 만료", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰 만료", "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_EXIST(HttpStatus.BAD_REQUEST, "리프레시 토큰 없음", "리프레시 토큰이 존재하지 않습니다."),


    // 403 권한오류
    CAN_NOT_ACCESS(HttpStatus.FORBIDDEN, "권한이 없습니다", "다른 사람이 작성한 글에 접근할 수 없습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다", "ADMIN 계정만 접근이 가능합니다"),


    // 500번대
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류", "서버에서 오류가 발생했습니다."),
    ANNOTATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "애노테이션 에러", "애노테이션 에러입니다.");

    private final HttpStatus httpStatus;
    private final String message;
    private final String detail;


    CustomMessage(HttpStatus httpStatus, String message, String detail) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.detail = detail;
    }

}
