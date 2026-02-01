package com.finance.planner.analysis.export.controller;

import com.finance.planner.analysis.export.service.ExportService;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Data export endpoints")
public class ExportController {

    private final ExportService exportService;
    private final UserRepository userRepository;

    @GetMapping("/transactions/excel")
    @Operation(summary = "Export transactions to Excel")
    public ResponseEntity<byte[]> exportTransactionsExcel(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Short type,
            @RequestParam(required = false) Long categoryId) {
        Long userId = getUserId(userDetails);
        byte[] data = exportService.exportTransactionsToExcel(userId, startDate, endDate, type, categoryId);
        String filename = "交易记录_" + startDate + "_" + endDate + ".xlsx";
        return buildDownloadResponse(data, filename);
    }

    @GetMapping("/transactions/csv")
    @Operation(summary = "Export transactions to CSV")
    public ResponseEntity<byte[]> exportTransactionsCsv(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Short type,
            @RequestParam(required = false) Long categoryId) {
        Long userId = getUserId(userDetails);
        byte[] data = exportService.exportTransactionsToCsv(userId, startDate, endDate, type, categoryId);
        String filename = "交易记录_" + startDate + "_" + endDate + ".csv";
        return buildDownloadResponse(data, filename);
    }

    @GetMapping("/report/monthly")
    @Operation(summary = "Export monthly financial report")
    public ResponseEntity<byte[]> exportMonthlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = getUserId(userDetails);
        byte[] data = exportService.exportMonthlyReport(userId, year, month);
        String filename = "月度报表_" + year + "-" + String.format("%02d", month) + ".xlsx";
        return buildDownloadResponse(data, filename);
    }

    @GetMapping("/report/annual")
    @Operation(summary = "Export annual financial report")
    public ResponseEntity<byte[]> exportAnnualReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year) {
        Long userId = getUserId(userDetails);
        byte[] data = exportService.exportAnnualReport(userId, year);
        String filename = "年度报表_" + year + ".xlsx";
        return buildDownloadResponse(data, filename);
    }

    private ResponseEntity<byte[]> buildDownloadResponse(byte[] data, String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
