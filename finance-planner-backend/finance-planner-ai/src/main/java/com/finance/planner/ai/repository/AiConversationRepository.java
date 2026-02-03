package com.finance.planner.ai.repository;

import com.finance.planner.ai.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);

    List<AiConversation> findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(Long userId, String sessionId);

    List<AiConversation> findTop30ByUserIdAndSessionIdOrderByCreatedAtDesc(Long userId, String sessionId);

    long countByUserIdAndRoleAndCreatedAtAfter(Long userId, String role, LocalDateTime after);
}
