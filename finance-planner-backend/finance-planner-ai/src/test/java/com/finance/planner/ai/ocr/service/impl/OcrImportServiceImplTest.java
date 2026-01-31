package com.finance.planner.ai.ocr.service.impl;

import com.finance.planner.accounting.dto.CreateTransactionRequest;
import com.finance.planner.accounting.dto.TransactionDto;
import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.ai.ocr.dto.OcrConfirmRequest;
import com.finance.planner.ai.ocr.dto.OcrPreviewDto;
import com.finance.planner.ai.ocr.entity.OcrRecord;
import com.finance.planner.ai.ocr.repository.OcrRecordRepository;
import com.finance.planner.ai.ocr.service.BaiduOcrService;
import com.finance.planner.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OcrImportServiceImpl Unit Tests")
class OcrImportServiceImplTest {

    @Mock
    private BaiduOcrService baiduOcrService;

    @Mock
    private OcrRecordRepository ocrRecordRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private OcrImportServiceImpl ocrImportService;

    private static final Long USER_ID = 1L;
    private static final Long RECORD_ID = 10L;

    // ========== uploadAndRecognize ==========

    @Test
    @DisplayName("uploadAndRecognize - success with amount and date extraction")
    void uploadAndRecognize_success() {
        String ocrText = "超市购物\n2025年01月15日\n合计: ¥128.50";
        Category category = new Category();
        category.setId(5L);
        category.setName("购物");

        when(baiduOcrService.recognizeText(any())).thenReturn(ocrText);
        when(categoryRepository.findAllByTypeOrderBySortOrderAsc((short) 2))
                .thenReturn(List.of(category));
        when(ocrRecordRepository.save(any(OcrRecord.class))).thenAnswer(invocation -> {
            OcrRecord r = invocation.getArgument(0);
            r.setId(RECORD_ID);
            return r;
        });
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        OcrPreviewDto result = ocrImportService.uploadAndRecognize(USER_ID, "receipt.jpg", new byte[]{1, 2, 3});

        assertThat(result).isNotNull();
        assertThat(result.getExtractedAmount()).isEqualByComparingTo(new BigDecimal("128.50"));
        assertThat(result.getExtractedDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(result.getStatus()).isEqualTo((short) 1);
        verify(baiduOcrService).recognizeText(any());
        verify(ocrRecordRepository).save(any(OcrRecord.class));
    }

    @Test
    @DisplayName("uploadAndRecognize - throws when OCR returns empty")
    void uploadAndRecognize_emptyResult() {
        when(baiduOcrService.recognizeText(any())).thenReturn("");

        assertThatThrownBy(() -> ocrImportService.uploadAndRecognize(USER_ID, "file.jpg", new byte[]{1}))
                .isInstanceOf(BusinessException.class);
        verify(ocrRecordRepository, never()).save(any());
    }

    // ========== confirmOcrRecord ==========

    @Test
    @DisplayName("confirmOcrRecord - success creates transaction")
    void confirmOcrRecord_success() {
        OcrRecord record = new OcrRecord();
        record.setId(RECORD_ID);
        record.setUserId(USER_ID);
        record.setStatus((short) 1);

        TransactionDto txDto = TransactionDto.builder().id(100L).build();

        when(ocrRecordRepository.findByIdAndUserId(RECORD_ID, USER_ID)).thenReturn(Optional.of(record));
        when(transactionService.createTransaction(eq(USER_ID), any(CreateTransactionRequest.class)))
                .thenReturn(txDto);
        when(ocrRecordRepository.save(any(OcrRecord.class))).thenAnswer(i -> i.getArgument(0));

        OcrConfirmRequest request = OcrConfirmRequest.builder()
                .amount(new BigDecimal("128.50"))
                .categoryId(5L)
                .transactionDate(LocalDate.of(2025, 1, 15))
                .merchant("超市")
                .build();

        OcrPreviewDto result = ocrImportService.confirmOcrRecord(USER_ID, RECORD_ID, request);

        assertThat(result.getStatus()).isEqualTo((short) 2);
        assertThat(record.getTransactionId()).isEqualTo(100L);
        verify(transactionService).createTransaction(eq(USER_ID), any(CreateTransactionRequest.class));
    }

    @Test
    @DisplayName("confirmOcrRecord - throws when already confirmed")
    void confirmOcrRecord_alreadyConfirmed() {
        OcrRecord record = new OcrRecord();
        record.setId(RECORD_ID);
        record.setUserId(USER_ID);
        record.setStatus((short) 2);

        when(ocrRecordRepository.findByIdAndUserId(RECORD_ID, USER_ID)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> ocrImportService.confirmOcrRecord(USER_ID, RECORD_ID, new OcrConfirmRequest()))
                .isInstanceOf(BusinessException.class);
        verify(transactionService, never()).createTransaction(any(), any());
    }

    @Test
    @DisplayName("confirmOcrRecord - throws when not found")
    void confirmOcrRecord_notFound() {
        when(ocrRecordRepository.findByIdAndUserId(RECORD_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ocrImportService.confirmOcrRecord(USER_ID, RECORD_ID, new OcrConfirmRequest()))
                .isInstanceOf(BusinessException.class);
    }

    // ========== rejectOcrRecord ==========

    @Test
    @DisplayName("rejectOcrRecord - success")
    void rejectOcrRecord_success() {
        OcrRecord record = new OcrRecord();
        record.setId(RECORD_ID);
        record.setUserId(USER_ID);
        record.setStatus((short) 1);

        when(ocrRecordRepository.findByIdAndUserId(RECORD_ID, USER_ID)).thenReturn(Optional.of(record));
        when(ocrRecordRepository.save(any(OcrRecord.class))).thenAnswer(i -> i.getArgument(0));

        ocrImportService.rejectOcrRecord(USER_ID, RECORD_ID);

        assertThat(record.getStatus()).isEqualTo((short) 3);
        verify(ocrRecordRepository).save(record);
    }

    @Test
    @DisplayName("rejectOcrRecord - throws when already confirmed")
    void rejectOcrRecord_alreadyConfirmed() {
        OcrRecord record = new OcrRecord();
        record.setId(RECORD_ID);
        record.setUserId(USER_ID);
        record.setStatus((short) 2);

        when(ocrRecordRepository.findByIdAndUserId(RECORD_ID, USER_ID)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> ocrImportService.rejectOcrRecord(USER_ID, RECORD_ID))
                .isInstanceOf(BusinessException.class);
    }

    // ========== getPendingRecords ==========

    @Test
    @DisplayName("getPendingRecords - returns pending records")
    void getPendingRecords_success() {
        OcrRecord record = new OcrRecord();
        record.setId(1L);
        record.setUserId(USER_ID);
        record.setStatus((short) 1);

        when(ocrRecordRepository.findByUserIdAndStatusOrderByCreatedAtDesc(USER_ID, (short) 1))
                .thenReturn(List.of(record));

        List<OcrPreviewDto> result = ocrImportService.getPendingRecords(USER_ID);

        assertThat(result).hasSize(1);
    }
}
