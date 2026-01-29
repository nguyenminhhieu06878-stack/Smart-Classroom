package com.smartclass.repository;

import com.smartclass.model.Classroom;
import com.smartclass.model.Lesson;
import com.smartclass.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByTeacher(User teacher);
    List<Lesson> findByTeacherAndSubject(User teacher, String subject);
    List<Lesson> findByClassroom(Classroom classroom);
    List<Lesson> findByClassroomIsNull(); // Bài giảng không gán phòng
    long countByTeacher(User teacher);
    
    @Query("SELECT l FROM Lesson l WHERE l.classroom IN :classrooms OR l.classroom IS NULL")
    List<Lesson> findByClassroomsOrNoClassroom(@Param("classrooms") List<Classroom> classrooms);
}
