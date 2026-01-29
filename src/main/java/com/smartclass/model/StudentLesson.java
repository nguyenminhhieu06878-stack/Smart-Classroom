package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    @Column(name = "completed")
    private Boolean completed = false;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
