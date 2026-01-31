package com.finance.planner.ai.ocr.repository;

import com.finance.planner.ai.ocr.entity.OcrRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcrRecordRepository extends JpaRepository<OcrRecord, Long> {

    List<OcrRecord> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Short status);

    Optional<OcrRecord> findByIdAndUserId(Long id, Long userId);
}
