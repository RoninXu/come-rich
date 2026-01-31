package com.finance.planner.ai.ocr.service.impl;

import com.finance.planner.accounting.dto.CreateTransactionRequest;
import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.ai.ocr.dto.OcrConfirmRequest;
import com.finance.planner.ai.ocr.dto.OcrPreviewDto;
import com.finance.planner.ai.ocr.entity.OcrRecord;
import com.finance.planner.ai.ocr.repository.OcrRecordRepository;
import com.finance.planner.ai.ocr.service.BaiduOcrService;
import com.finance.planner.ai.ocr.service.OcrImportService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrImportServiceImpl implements OcrImportService {

    private final BaiduOcrService baiduOcrService;
    private final OcrRecordRepository ocrRecordRepository;
    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?:¥|￥|CNY|RMB|合计|总计|实付|应付|金额)[:\\s]*([\\d,]+\\.?\\d{0,2})|([\\d,]+\\.\\d{2})(?:\\s*元)"
    );
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(\\d{4})[年/\\-](\\d{1,2})[月/\\-](\\d{1,2})[日号]?"
    );

    @Override
    @Transactional
    public OcrPreviewDto uploadAndRecognize(Long userId, String filename, byte[] imageBytes) {
        // 1. OCR recognition
        String rawText = baiduOcrService.recognizeText(imageBytes);
        if (rawText == null || rawText.isBlank()) {
            throw new BusinessException(ErrorCode.OCR_EMPTY_RESULT);
        }

        // 2. Extract structured data
        BigDecimal amount = extractAmount(rawText);
        String merchant = extractMerchant(rawText);
        LocalDate date = extractDate(rawText);

        // 3. Try to match a category (default to first expense category)
        Long categoryId = suggestCategory(rawText);

        // 4. Save OCR record
        OcrRecord record = new OcrRecord();
        record.setUserId(userId);
        record.setOriginalFilename(filename);
        record.setOcrRawText(rawText);
        record.setExtractedAmount(amount);
        record.setExtractedMerchant(merchant);
        record.setExtractedDate(date != null ? date : LocalDate.now());
        record.setSuggestedCategoryId(categoryId);
        record.setStatus((short) 1);

        OcrRecord saved = ocrRecordRepository.save(record);
        log.info("Created OCR record {} for user {}", saved.getId(), userId);

        OcrPreviewDto dto = OcrPreviewDto.fromEntity(saved);
        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .ifPresent(c -> dto.setSuggestedCategoryName(c.getName()));
        }
        return dto;
    }

    @Override
    @Transactional
    public OcrPreviewDto confirmOcrRecord(Long userId, Long recordId, OcrConfirmRequest request) {
        OcrRecord record = ocrRecordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OCR_RECORD_NOT_FOUND));

        if (record.getStatus() == 2) {
            throw new BusinessException(ErrorCode.OCR_ALREADY_CONFIRMED);
        }
        if (record.getStatus() == 3) {
            throw new BusinessException(ErrorCode.OCR_ALREADY_REJECTED);
        }

        // Create transaction from confirmed data
        CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                .amount(request.getAmount())
                .type((short) 2) // OCR is for expense receipts
                .categoryId(request.getCategoryId())
                .description(request.getDescription() != null ? request.getDescription() : "OCR导入")
                .transactionDate(request.getTransactionDate())
                .merchant(request.getMerchant())
                .build();

        var transactionDto = transactionService.createTransaction(userId, txRequest);

        // Update OCR record
        record.setStatus((short) 2);
        record.setTransactionId(transactionDto.getId());
        ocrRecordRepository.save(record);

        log.info("Confirmed OCR record {} → transaction {} for user {}", recordId, transactionDto.getId(), userId);
        return OcrPreviewDto.fromEntity(record);
    }

    @Override
    @Transactional
    public void rejectOcrRecord(Long userId, Long recordId) {
        OcrRecord record = ocrRecordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OCR_RECORD_NOT_FOUND));

        if (record.getStatus() == 2) {
            throw new BusinessException(ErrorCode.OCR_ALREADY_CONFIRMED);
        }

        record.setStatus((short) 3);
        ocrRecordRepository.save(record);
        log.info("Rejected OCR record {} for user {}", recordId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcrPreviewDto> getPendingRecords(Long userId) {
        List<OcrRecord> records = ocrRecordRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, (short) 1);
        return records.stream().map(record -> {
            OcrPreviewDto dto = OcrPreviewDto.fromEntity(record);
            if (record.getSuggestedCategoryId() != null) {
                categoryRepository.findById(record.getSuggestedCategoryId())
                        .ifPresent(c -> dto.setSuggestedCategoryName(c.getName()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private BigDecimal extractAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        BigDecimal maxAmount = null;
        while (matcher.find()) {
            String amountStr = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (amountStr != null) {
                try {
                    BigDecimal amount = new BigDecimal(amountStr.replace(",", ""));
                    if (maxAmount == null || amount.compareTo(maxAmount) > 0) {
                        maxAmount = amount;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return maxAmount;
    }

    private LocalDate extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                return LocalDate.of(year, month, day);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private String extractMerchant(String text) {
        // Simple heuristic: first line that's not a number/date is likely the merchant
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() >= 2 && trimmed.length() <= 50
                    && !trimmed.matches(".*\\d{4}[年/\\-].*")
                    && !trimmed.matches("[\\d¥￥,\\.\\s]+")
                    && !trimmed.matches(".*(合计|总计|实付|找零|现金|刷卡|微信|支付宝).*")) {
                return trimmed;
            }
        }
        return null;
    }

    private Long suggestCategory(String text) {
        // Simple keyword matching against expense categories
        List<Category> expenseCategories = categoryRepository.findAllByTypeOrderBySortOrderAsc((short) 2);
        String lowerText = text.toLowerCase();
        for (Category category : expenseCategories) {
            if (lowerText.contains(category.getName().toLowerCase())) {
                return category.getId();
            }
        }
        // Default: return first expense category if available
        return expenseCategories.isEmpty() ? null : expenseCategories.get(0).getId();
    }
}
