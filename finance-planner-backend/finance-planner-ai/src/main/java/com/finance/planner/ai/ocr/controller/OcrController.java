package com.finance.planner.ai.ocr.controller;

import com.finance.planner.ai.ocr.dto.OcrConfirmRequest;
import com.finance.planner.ai.ocr.dto.OcrPreviewDto;
import com.finance.planner.ai.ocr.service.OcrImportService;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/accounting/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR Import", description = "OCR bill import endpoints")
public class OcrController {

    private final OcrImportService ocrImportService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    @Operation(summary = "Upload receipt image for OCR recognition")
    public ApiResponse<OcrPreviewDto> uploadReceipt(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = getUserId(userDetails);

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择文件");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.OCR_FILE_TOO_LARGE);
        }

        OcrPreviewDto result = ocrImportService.uploadAndRecognize(
                userId, file.getOriginalFilename(), file.getBytes());
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm OCR result and create transaction")
    public ApiResponse<OcrPreviewDto> confirmRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody OcrConfirmRequest request) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(ocrImportService.confirmOcrRecord(userId, id, request));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject OCR result")
    public ApiResponse<Void> rejectRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = getUserId(userDetails);
        ocrImportService.rejectOcrRecord(userId, id);
        return ApiResponse.success();
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending OCR records")
    public ApiResponse<List<OcrPreviewDto>> getPendingRecords(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ApiResponse.success(ocrImportService.getPendingRecords(userId));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
