package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    // Features
    @Column(name = "max_classes")
    private Integer maxClasses;
    
    @Column(name = "max_students")
    private Integer maxStudents;
    
    @Column(name = "ai_lessons_per_month")
    private Integer aiLessonsPerMonth;
    
    @Column(name = "tests_per_month")
    private Integer testsPerMonth;
    
    @Column(name = "max_questions_bank")
    private Integer maxQuestionsBank;
    
    @Column(name = "auto_grading")
    private Boolean autoGrading = false;
    
    @Column(name = "advanced_reports")
    private Boolean advancedReports = false;
    
    @Column(nullable = false)
    private Boolean active = true;
}
