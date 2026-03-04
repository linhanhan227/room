package com.chat.room.service;

import com.chat.room.dto.HandleReportRequest;
import com.chat.room.dto.ReportDTO;
import com.chat.room.dto.ReportRequest;
import com.chat.room.entity.Report;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.exception.ResourceNotFoundException;
import com.chat.room.repository.ChatRoomRepository;
import com.chat.room.repository.MessageRepository;
import com.chat.room.repository.ReportRepository;
import com.chat.room.repository.UserRepository;
import com.chat.room.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    private static final int MAX_REPORTS_PER_DAY = 10;

    @Transactional
    public ReportDTO createReport(ReportRequest request) {
        User currentUser = getCurrentUser();

        validateReportRequest(request, currentUser.getId());

        Report report = Report.builder()
                .reporterId(currentUser.getId())
                .reportedUserId(request.getReportedUserId())
                .reportedRoomId(request.getReportedRoomId())
                .reportedMessageId(request.getReportedMessageId())
                .type(parseReportType(request.getType()))
                .targetType(parseTargetType(request.getTargetType()))
                .reason(request.getReason())
                .description(request.getDescription())
                .status(Report.ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);
        log.info("User {} created report {} for {}", currentUser.getId(), report.getId(), request.getTargetType());

        return enrichReportDTO(ReportDTO.fromEntity(report));
    }

    private void validateReportRequest(ReportRequest request, Long reporterId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = reportRepository.countByReporterIdSince(reporterId, todayStart);
        if (todayCount >= MAX_REPORTS_PER_DAY) {
            throw new BusinessException("今日举报次数已达上限");
        }

        Report.TargetType targetType = parseTargetType(request.getTargetType());

        switch (targetType) {
            case USER:
                if (request.getReportedUserId() == null) {
                    throw new BusinessException("举报用户时必须指定用户ID");
                }
                if (!userRepository.existsById(request.getReportedUserId())) {
                    throw new ResourceNotFoundException("被举报用户不存在");
                }
                if (request.getReportedUserId().equals(reporterId)) {
                    throw new BusinessException("不能举报自己");
                }
                if (reportRepository.existsByReporterIdAndReportedUserIdAndStatus(
                        reporterId, request.getReportedUserId(), Report.ReportStatus.PENDING)) {
                    throw new BusinessException("该用户已有待处理的举报");
                }
                break;

            case ROOM:
                if (request.getReportedRoomId() == null) {
                    throw new BusinessException("举报聊天室时必须指定聊天室ID");
                }
                if (!chatRoomRepository.existsById(request.getReportedRoomId())) {
                    throw new ResourceNotFoundException("被举报聊天室不存在");
                }
                break;

            case MESSAGE:
                if (request.getReportedMessageId() == null) {
                    throw new BusinessException("举报消息时必须指定消息ID");
                }
                if (!messageRepository.existsById(request.getReportedMessageId())) {
                    throw new ResourceNotFoundException("被举报消息不存在");
                }
                if (reportRepository.existsByReporterIdAndReportedMessageIdAndStatus(
                        reporterId, request.getReportedMessageId(), Report.ReportStatus.PENDING)) {
                    throw new BusinessException("该消息已有待处理的举报");
                }
                break;
        }
    }

    @Transactional
    public ReportDTO handleReport(Long reportId, HandleReportRequest request) {
        User currentUser = getCurrentUser();

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("举报记录不存在"));

        if (report.getStatus() == Report.ReportStatus.RESOLVED || 
            report.getStatus() == Report.ReportStatus.REJECTED) {
            throw new BusinessException("该举报已处理完成");
        }

        report.setStatus(parseReportStatus(request.getStatus()));
        report.setHandlerId(currentUser.getId());
        report.setHandleResult(request.getHandleResult());
        report.setHandledAt(LocalDateTime.now());

        report = reportRepository.save(report);
        log.info("Admin {} handled report {} with status {}", 
                currentUser.getId(), reportId, request.getStatus());

        return enrichReportDTO(ReportDTO.fromEntity(report));
    }

    public ReportDTO getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("举报记录不存在"));
        return enrichReportDTO(ReportDTO.fromEntity(report));
    }

    public Page<ReportDTO> getReports(Pageable pageable) {
        return reportRepository.findAll(pageable)
                .map(report -> enrichReportDTO(ReportDTO.fromEntity(report)));
    }

    public Page<ReportDTO> getReportsByStatus(String status, Pageable pageable) {
        return reportRepository.findByStatus(parseReportStatus(status), pageable)
                .map(report -> enrichReportDTO(ReportDTO.fromEntity(report)));
    }

    public Page<ReportDTO> getReportsByType(String type, Pageable pageable) {
        return reportRepository.findByType(parseReportType(type), pageable)
                .map(report -> enrichReportDTO(ReportDTO.fromEntity(report)));
    }

    public Page<ReportDTO> getMyReports(Pageable pageable) {
        User currentUser = getCurrentUser();
        return reportRepository.findByReporterId(currentUser.getId(), pageable)
                .map(report -> enrichReportDTO(ReportDTO.fromEntity(report)));
    }

    public Map<String, Object> getReportStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", reportRepository.count());
        stats.put("pending", reportRepository.countByStatus().stream()
                .filter(o -> o[0] == Report.ReportStatus.PENDING)
                .mapToLong(o -> (Long) o[1])
                .sum());
        stats.put("processing", reportRepository.countByStatus().stream()
                .filter(o -> o[0] == Report.ReportStatus.PROCESSING)
                .mapToLong(o -> (Long) o[1])
                .sum());
        stats.put("resolved", reportRepository.countByStatus().stream()
                .filter(o -> o[0] == Report.ReportStatus.RESOLVED)
                .mapToLong(o -> (Long) o[1])
                .sum());
        stats.put("rejected", reportRepository.countByStatus().stream()
                .filter(o -> o[0] == Report.ReportStatus.REJECTED)
                .mapToLong(o -> (Long) o[1])
                .sum());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.put("todayReports", reportRepository.countReportsSince(todayStart));

        return stats;
    }

    @Transactional
    public void deleteReport(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("举报记录不存在");
        }
        reportRepository.deleteById(reportId);
        log.info("Report {} deleted", reportId);
    }

    private ReportDTO enrichReportDTO(ReportDTO dto) {
        if (dto.getReporterId() != null) {
            userRepository.findById(dto.getReporterId())
                    .ifPresent(u -> dto.setReporterName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        }
        if (dto.getReportedUserId() != null) {
            userRepository.findById(dto.getReportedUserId())
                    .ifPresent(u -> dto.setReportedUserName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        }
        if (dto.getReportedRoomId() != null) {
            chatRoomRepository.findById(dto.getReportedRoomId())
                    .ifPresent(r -> dto.setReportedRoomName(r.getName()));
        }
        if (dto.getHandlerId() != null) {
            userRepository.findById(dto.getHandlerId())
                    .ifPresent(u -> dto.setHandlerName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        }
        return dto;
    }

    private Report.ReportType parseReportType(String type) {
        try {
            return Report.ReportType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的举报类型: " + type);
        }
    }

    private Report.TargetType parseTargetType(String targetType) {
        try {
            return Report.TargetType.valueOf(targetType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的举报目标类型: " + targetType);
        }
    }

    private Report.ReportStatus parseReportStatus(String status) {
        try {
            return Report.ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的举报状态: " + status);
        }
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
    }
}
