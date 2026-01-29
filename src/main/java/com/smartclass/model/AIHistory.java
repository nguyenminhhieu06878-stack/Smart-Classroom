package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_history")
@NoArgsConstructor
@AllArgsConstructor
public class AIHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "ai_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AIType aiType;

    @Column(name = "prompt", columnDefinition = "TEXT")
    private String prompt; // Input của user

    @Column(name = "response", columnDefinition = "TEXT")
    private String response; // Kết quả từ AI

    @Column(name = "subject")
    private String subject;

    @Column(name = "grade")
    private String grade;

    @Column(name = "topic")
    private String topic;

    @Column(name = "items_count")
    private Integer itemsCount; // Số câu hỏi/bài giảng đã tạo

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum AIType {
        QUESTIONS, // Tạo câu hỏi
        LESSON, // Tạo bài giảng
        TEST // Tạo đề thi
    }

    // Manual Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public AIType getAiType() {
        return aiType;
    }

    public void setAiType(AIType aiType) {
        this.aiType = aiType;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
