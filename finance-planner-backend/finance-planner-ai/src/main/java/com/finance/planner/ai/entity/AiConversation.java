package com.finance.planner.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_conversation", indexes = {
        @Index(name = "idx_ai_conversation_user_id", columnList = "user_id"),
        @Index(name = "idx_ai_conversation_session_id", columnList = "session_id"),
        @Index(name = "idx_ai_conversation_user_session", columnList = "user_id, session_id"),
        @Index(name = "idx_ai_conversation_created_at", columnList = "created_at")
})
public class AiConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private Integer tokens;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
