package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    
    @Column(name = "amount")
    private Double amount;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    // AI Usage Tracking
    @Column(name = "ai_lessons_used")
    private Integer aiLessonsUsed = 0;
    
    @Column(name = "ai_questions_used")
    private Integer aiQuestionsUsed = 0;
    
    @Column(name = "ai_tests_used")
    private Integer aiTestsUsed = 0;
    
    @Column(name = "tests_created")
    private Integer testsCreated = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum Status {
        ACTIVE,
        EXPIRED,
        CANCELLED
    }
    
    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED,
        REFUNDED
    }
    
    // Helper methods for usage tracking
    public void incrementAiLessonsUsed() {
        this.aiLessonsUsed = (this.aiLessonsUsed == null ? 0 : this.aiLessonsUsed) + 1;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementAiQuestionsUsed(int count) {
        this.aiQuestionsUsed = (this.aiQuestionsUsed == null ? 0 : this.aiQuestionsUsed) + count;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementAiTestsUsed() {
        this.aiTestsUsed = (this.aiTestsUsed == null ? 0 : this.aiTestsUsed) + 1;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementTestsCreated() {
        this.testsCreated = (this.testsCreated == null ? 0 : this.testsCreated) + 1;
        this.updatedAt = LocalDateTime.now();
    }
    
    public long getDaysRemaining() {
        if (endDate == null) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDate)) return 0;
        return java.time.Duration.between(now, endDate).toDays();
    }
}
