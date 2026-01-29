package com.smartclass.model;

import jakarta.persistence.*;

@Entity
@Table(name = "test_questions")
public class TestQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "custom_points")
    private Double customPoints;

    public TestQuestion() {
    }

    public TestQuestion(Long id, Test test, Question question, Integer orderNumber, Double customPoints) {
        this.id = id;
        this.test = test;
        this.question = question;
        this.orderNumber = orderNumber;
        this.customPoints = customPoints;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Double getCustomPoints() {
        return customPoints;
    }

    public void setCustomPoints(Double customPoints) {
        this.customPoints = customPoints;
    }
}
