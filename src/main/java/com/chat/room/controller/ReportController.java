package com.chat.room.controller;

import com.chat.room.dto.ApiResponse;
import com.chat.room.dto.HandleReportRequest;
import com.chat.room.dto.ReportDTO;
import com.chat.room.dto.ReportRequest;
import com.chat.room.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportDTO>> createReport(@Valid @RequestBody ReportRequest request) {
        ReportDTO report = reportService.createReport(request);
        return ResponseEntity.ok(ApiResponse.success("举报提交成功", report));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ReportDTO>>> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReportDTO> reports = reportService.getMyReports(pageable);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDTO>> getReportById(@PathVariable Long id) {
        ReportDTO report = reportService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReportDTO>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReportDTO> reports = reportService.getReports(pageable);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<ReportDTO>>> getReportsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReportDTO> reports = reportService.getReportsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Page<ReportDTO>>> getReportsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReportDTO> reports = reportService.getReportsByType(type, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Map<String, Object> stats = reportService.getReportStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/handle")
    public ResponseEntity<ApiResponse<ReportDTO>> handleReport(
            @PathVariable Long id,
            @Valid @RequestBody HandleReportRequest request) {
        ReportDTO report = reportService.handleReport(id, request);
        return ResponseEntity.ok(ApiResponse.success("举报处理成功", report));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("举报记录已删除", null));
    }
}
