package com.finance.planner.ai.ocr.service;

import com.finance.planner.ai.ocr.dto.OcrConfirmRequest;
import com.finance.planner.ai.ocr.dto.OcrPreviewDto;

import java.util.List;

public interface OcrImportService {

    /**
     * Upload and recognize receipt image
     */
    OcrPreviewDto uploadAndRecognize(Long userId, String filename, byte[] imageBytes);

    /**
     * Confirm OCR result and create transaction
     */
    OcrPreviewDto confirmOcrRecord(Long userId, Long recordId, OcrConfirmRequest request);

    /**
     * Reject OCR result
     */
    void rejectOcrRecord(Long userId, Long recordId);

    /**
     * List pending OCR records for user
     */
    List<OcrPreviewDto> getPendingRecords(Long userId);
}
