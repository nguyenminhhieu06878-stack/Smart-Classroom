package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;
    
    @Column(nullable = false)
    private String title;
    
    private String subject;
    private String grade;
    
    @Column(name = "test_matrix", columnDefinition = "TEXT")
    private String testMatrix; // JSON format
    
    @Column(name = "total_points")
    private Double totalPoints;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL)
    private List<TestQuestion> testQuestions = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
