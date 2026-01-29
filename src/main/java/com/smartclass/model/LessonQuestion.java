package com.smartclass.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lesson_questions")
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

    public LessonQuestion() {
    }

    public LessonQuestion(Long id, Lesson lesson, Question question, Section section, Integer orderNumber) {
        this.id = id;
        this.lesson = lesson;
        this.question = question;
        this.section = section;
        this.orderNumber = orderNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public enum Section {
        EXAMPLE, PRACTICE, HOMEWORK
    }
}
