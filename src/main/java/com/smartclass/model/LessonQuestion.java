package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lesson_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Section section;
    
    @Column(name = "order_number")
    private Integer orderNumber;
    
    public enum Section {
        EXAMPLE, PRACTICE, HOMEWORK
    }
}
