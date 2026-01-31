package com.finance.planner.ai.ocr.dto;

import com.finance.planner.ai.ocr.entity.OcrRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrPreviewDto {

    private Long id;
    private String originalFilename;
    private String ocrRawText;
    private BigDecimal extractedAmount;
    private String extractedMerchant;
    private LocalDate extractedDate;
    private Long suggestedCategoryId;
    private String suggestedCategoryName;
    private Short status;
    private LocalDateTime createdAt;

    public static OcrPreviewDto fromEntity(OcrRecord record) {
        return OcrPreviewDto.builder()
                .id(record.getId())
                .originalFilename(record.getOriginalFilename())
                .ocrRawText(record.getOcrRawText())
                .extractedAmount(record.getExtractedAmount())
                .extractedMerchant(record.getExtractedMerchant())
                .extractedDate(record.getExtractedDate())
                .suggestedCategoryId(record.getSuggestedCategoryId())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
