package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_history")
@Data
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
        QUESTIONS,  // Tạo câu hỏi
        LESSON,     // Tạo bài giảng
        TEST        // Tạo đề thi
    }
}
