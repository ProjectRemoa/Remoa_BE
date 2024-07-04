package Remoa.BE.Web.Inquiry.Service;

import Remoa.BE.Web.Inquiry.Domain.Inquiry;
import Remoa.BE.Web.Inquiry.Dto.Req.ReqInquiryDto;
import Remoa.BE.Web.Inquiry.Dto.Res.ResInquiryDetailDto;
import Remoa.BE.Web.Inquiry.Dto.Res.ResInquiryDto;
import Remoa.BE.Web.Inquiry.Dto.Res.ResInquiryPaging;
import Remoa.BE.Web.Inquiry.Repository.InquiryRepository;
import Remoa.BE.Web.Member.Domain.Member;
import Remoa.BE.Web.Post.Domain.Post;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InquiryService {

    private static final int INQUIRY_NUMBER = 5;

    private final InquiryRepository inquiryRepository;

    @Transactional
    public void registerInquiry(ReqInquiryDto reqInquiryDto, String enrollNickname) {

        inquiryRepository.save(reqInquiryDto.toEntityInquiry(enrollNickname));
    }

    @Transactional
    public void deleteInquiry(Long inquiryId, Member member) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));

        // 공지를 작성한 회원과 현재 로그인한 회원이 같은 경우에만 삭제를 허용합니다.
        if (!inquiry.getAuthor().equals(member.getNickname())) {
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }
        inquiryRepository.delete(inquiry);
    }

    @Transactional
    public void updateInquiry(Long inquiryId, ReqInquiryDto inquiryDto, Member member) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BaseException(CustomMessage.NO_ID));

        // 문의를 작성한 회원과 현재 로그인한 회원이 같은 경우에만 수정을 허용합니다.
        if (!inquiry.getAuthor().equals(member.getNickname())) {
            throw new BaseException(CustomMessage.CAN_NOT_ACCESS);
        }
        // 요청으로 받은 데이터로 문의를 업데이트합니다.
        inquiry.updateInquiry(inquiryDto);
    }

    public ResInquiryPaging getInquiry(int pageNumber) {

        Page<Inquiry> inquiries = inquiryRepository.findAll(PageRequest.of(pageNumber, INQUIRY_NUMBER, Sort.by("postingTime").descending()));
        List<ResInquiryDto> contents = inquiries.stream().map(ResInquiryDto::new).toList();

        ResInquiryPaging resInquiryPaging = ResInquiryPaging.builder()
                .inquiries(contents)
                .totalPages(inquiries.getTotalPages())
                .totalOfAllInquiries(inquiries.getTotalElements())
                .totalOfPageElements(inquiries.getNumberOfElements())
                .build();

        return resInquiryPaging;
    }

    public ResInquiryDetailDto getInquiryView(int inquiryId, HttpSession session) {
        Inquiry inquiry = inquiryRepository.findById((long) inquiryId).orElseThrow(() ->
                new BaseException(CustomMessage.NO_ID));
        handleViewCount(inquiry, session);
        return new ResInquiryDetailDto(inquiry);
    }


    @Transactional
    public void modifying_Inquiry_NickName(String newNick, String oldNick) {
        inquiryRepository.modifyingInquiryAuthor(newNick, oldNick);
    }

    private void handleViewCount(Inquiry inquiry, HttpSession session) {
        Long inquiryId = inquiry.getInquiryId();

        String sessionKey = "InquiryViewed" + inquiryId;
        log.info("sessionKey = {}", sessionKey);

        if (session.getAttribute(sessionKey) == null) {
            inquiry.addViewCount();
            session.setAttribute(sessionKey, true);
        }
    }
}
