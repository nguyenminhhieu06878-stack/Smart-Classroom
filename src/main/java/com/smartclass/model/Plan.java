package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plans")
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

    // Manual Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getMaxClasses() {
        return maxClasses;
    }

    public void setMaxClasses(Integer maxClasses) {
        this.maxClasses = maxClasses;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public Integer getAiLessonsPerMonth() {
        return aiLessonsPerMonth;
    }

    public void setAiLessonsPerMonth(Integer aiLessonsPerMonth) {
        this.aiLessonsPerMonth = aiLessonsPerMonth;
    }

    public Integer getTestsPerMonth() {
        return testsPerMonth;
    }

    public void setTestsPerMonth(Integer testsPerMonth) {
        this.testsPerMonth = testsPerMonth;
    }

    public Integer getMaxQuestionsBank() {
        return maxQuestionsBank;
    }

    public void setMaxQuestionsBank(Integer maxQuestionsBank) {
        this.maxQuestionsBank = maxQuestionsBank;
    }

    public Boolean getAutoGrading() {
        return autoGrading;
    }

    public void setAutoGrading(Boolean autoGrading) {
        this.autoGrading = autoGrading;
    }

    public Boolean getAdvancedReports() {
        return advancedReports;
    }

    public void setAdvancedReports(Boolean advancedReports) {
        this.advancedReports = advancedReports;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
