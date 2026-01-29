package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;
    
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;
    
    // Cho câu hỏi trắc nghiệm
    @Column(name = "option_a", columnDefinition = "TEXT")
    private String optionA;
    
    @Column(name = "option_b", columnDefinition = "TEXT")
    private String optionB;
    
    @Column(name = "option_c", columnDefinition = "TEXT")
    private String optionC;
    
    @Column(name = "option_d", columnDefinition = "TEXT")
    private String optionD;
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;
    
    private String subject;
    private String grade;
    private String topic;
    
    @Column(name = "reference_book")
    private String referenceBook;
    
    @Column(name = "reference_page")
    private String referencePage;
    
    private Double points = 1.0;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "created_by")
    private CreatedBy createdBy = CreatedBy.MANUAL;
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum QuestionType {
        MULTIPLE_CHOICE, ESSAY, TRUE_FALSE, FILL_BLANK
    }
    
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    
    public enum CreatedBy {
        MANUAL, AI, IMPORT
    }
}
