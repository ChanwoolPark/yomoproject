package com.project.yomozomo.service;

import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.ChatroomReport;
import com.project.yomozomo.repository.ChatRoomRepository;
import com.project.yomozomo.repository.ChatroomReportRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatroomReportService {

    private final ChatroomReportRepository chatroomReportRepository;
    private final RentalRepository rentalRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final WalletService walletService;
    private final WalletLogService walletLogService;
    private final UserWalletRepository userWalletRepository;

    public void submitReport(Long chatRoomId, Long reporterId, Long reportedId, String title, String content) {
        ChatroomReport report = new ChatroomReport();
        report.setChatRoomId(chatRoomId);
        report.setReporterId(reporterId);
        report.setReportedId(reportedId);
        report.setTitle(title);
        report.setContent(content);
        report.setCreatedAt(new Date());
        report.setStatus("처리중");
        chatroomReportRepository.save(report);
    }

    public List<ChatroomReport> getAllReports() {
        return chatroomReportRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<ChatroomReport> getReport(Long reportId) {
        return chatroomReportRepository.findById(reportId);
    }

    public void updateStatus(Long reportId, String status) {
        ChatroomReport report = chatroomReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 내역을 찾을 수 없습니다."));
        report.setStatus(status);
        chatroomReportRepository.save(report);
    }
    @Transactional
    public void processDeposit(Long reportId, Long receiverId, int amount, String rentalStatus) {
        ChatroomReport report = chatroomReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 내역을 찾을 수 없습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(report.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방이 없습니다."));
        Rental rental = rentalRepository.findById(chatRoom.getRental().getRentalId())
                .orElseThrow(() -> new RuntimeException("렌탈 정보가 없습니다."));

        // 보증금 정산: receiverId(구매자 또는 판매자)에게 금액 입금
        walletService.increaseBalance(receiverId, amount);

        // 렌탈 상태 변경
        rental.setStatus(rentalStatus);
        rentalRepository.save(rental);

        // 정산 내역, 처리 로그 등 추가 저장 가능
        // 1. receiverId로 wallet_id 찾기
        UserWallet userWallet = userWalletRepository.findByUserId(receiverId);

        // 2. wallet_id로 로그 저장
        walletLogService.saveLog(userWallet.getWalletId(), "신고/환불", amount);
    }
}
