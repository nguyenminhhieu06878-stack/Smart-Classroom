package com.smartclass.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classrooms")
public class Classroom {
    public Classroom() {
    }

    public Classroom(Long id, User teacher, String roomId, String name, String description, String subject,
            String grade, Boolean isActive, List<ClassroomMember> members, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.teacher = teacher;
        this.roomId = roomId;
        this.name = name;
        this.description = description;
        this.subject = subject;
        this.grade = grade;
        this.isActive = isActive;
        this.members = members;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "room_id", unique = true, nullable = false)
    private String roomId; // Mã phòng (ví dụ: TC001)

    @Column(nullable = false)
    private String name; // Tên lớp (ví dụ: Toán 1A)

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "subject")
    private String subject; // Môn học

    @Column(name = "grade")
    private String grade; // Lớp

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    private List<ClassroomMember> members = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Manual Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<ClassroomMember> getMembers() {
        return members;
    }

    public void setMembers(List<ClassroomMember> members) {
        this.members = members;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
