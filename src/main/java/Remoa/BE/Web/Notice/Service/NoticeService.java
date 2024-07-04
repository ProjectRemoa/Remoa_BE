package Remoa.BE.Web.Notice.Service;

import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Notice.Dto.Req.ReqNoticeDto;
import Remoa.BE.Web.Notice.Dto.Res.NoticeResponseDto;
import Remoa.BE.Web.Notice.Dto.Res.ResNoticeDetailDto;
import Remoa.BE.Web.Notice.Dto.Res.ResNoticeDto;
import Remoa.BE.Web.Notice.Repository.NoticeRepository;
import Remoa.BE.Web.Notice.domain.Notice;
import Remoa.BE.exception.CustomMessage;
import Remoa.BE.exception.response.BaseException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public void registerNotice(ReqNoticeDto reqNoticeDto, String enrollNickname) {
        noticeRepository.save(reqNoticeDto.toEntityNotice(enrollNickname)); //builder를 이용해 객체를 직접 생성하지 않고 Notice 저장
    }

    @Transactional
    public void updateNotice(Long noticeId, ReqNoticeDto reqNoticeDto, String enrollNickname) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));
        notice.updateNotice(reqNoticeDto, enrollNickname);
    }

    @Transactional
    public void deleteNotice(Long noticeId, Member member) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));

        // 공지를 작성한 회원과 현재 로그인한 회원이 같은 경우에만 삭제를 허용합니다.
        if (!notice.getAuthor().equals(member.getNickname())) {
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }
        noticeRepository.delete(notice);
    }

    public NoticeResponseDto getNotice(int pageNumber) {

        HashMap<String, Object> resultMap = new HashMap<>();

        int NOTICE_NUMBER = 5;

        Page<Notice> notices = noticeRepository.findAll(PageRequest.of(pageNumber, NOTICE_NUMBER, Sort.by("postingTime").descending()));

        List<ResNoticeDto> noticeDtos = notices.stream().map(ResNoticeDto::new).toList();

        return NoticeResponseDto.builder()
                .notices(noticeDtos)
                .totalPages(notices.getTotalPages())
                .totalOfAllNotices(notices.getTotalElements())
                .totalOfPageElements(notices.getNumberOfElements())
                .build();
    }

    @Transactional
    public ResNoticeDetailDto getNoticeView(int noticeId, HttpSession session) {
        Notice notice = noticeRepository.findById((long) noticeId).orElseThrow(() ->
                new BaseException(CustomMessage.NO_ID));

        handleViewCount(notice, session);
        return new ResNoticeDetailDto(notice);
    }

    private void handleViewCount(Notice notice, HttpSession session) {
        Long noticeId = notice.getNoticeId();

        String sessionKey = "NoticeViewed" + noticeId;
        log.info("sessionKey = {}", sessionKey);

        if (session.getAttribute(sessionKey) == null) {
            notice.addViewCount();
            session.setAttribute(sessionKey, true);
        }
    }

    @Transactional
    public void modifying_Notice_NickName(String newNick, String oldNick) {
        noticeRepository.modifyingNoticeAuthor(newNick, oldNick);
    }
}
