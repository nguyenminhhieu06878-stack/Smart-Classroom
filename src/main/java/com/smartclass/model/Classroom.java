package com.smartclass.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classrooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classroom {
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
}
