package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
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
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private String subject;
    private String grade;
    private String topic;
    
    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;
    
    @Column(name = "video_url")
    private String videoUrl;
    
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    private List<LessonQuestion> lessonQuestions = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
