package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "score")
    private Double score;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    
    @Column(name = "answers", columnDefinition = "TEXT")
    private String answers; // JSON format
    
    public enum Status {
        PENDING,    // Chưa làm
        IN_PROGRESS, // Đang làm
        SUBMITTED,   // Đã nộp
        GRADED      // Đã chấm điểm
    }
}
