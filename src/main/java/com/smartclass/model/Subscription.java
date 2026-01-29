package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
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

    // Manual Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getAiLessonsUsed() {
        return aiLessonsUsed;
    }

    public void setAiLessonsUsed(Integer aiLessonsUsed) {
        this.aiLessonsUsed = aiLessonsUsed;
    }

    public Integer getAiQuestionsUsed() {
        return aiQuestionsUsed;
    }

    public void setAiQuestionsUsed(Integer aiQuestionsUsed) {
        this.aiQuestionsUsed = aiQuestionsUsed;
    }

    public Integer getAiTestsUsed() {
        return aiTestsUsed;
    }

    public void setAiTestsUsed(Integer aiTestsUsed) {
        this.aiTestsUsed = aiTestsUsed;
    }

    public Integer getTestsCreated() {
        return testsCreated;
    }

    public void setTestsCreated(Integer testsCreated) {
        this.testsCreated = testsCreated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

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
        if (endDate == null)
            return 0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDate))
            return 0;
        return java.time.Duration.between(now, endDate).toDays();
    }
}
