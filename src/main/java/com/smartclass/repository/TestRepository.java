package com.smartclass.repository;

import com.smartclass.model.Classroom;
import com.smartclass.model.Test;
import com.smartclass.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByTeacher(User teacher);
    List<Test> findByTeacherAndSubject(User teacher, String subject);
    List<Test> findByClassroom(Classroom classroom);
    List<Test> findByClassroomIsNull(); // Đề thi không gán phòng
    long countByTeacher(User teacher);
    
    @Query("SELECT t FROM Test t WHERE t.classroom IN :classrooms OR t.classroom IS NULL")
    List<Test> findByClassroomsOrNoClassroom(@Param("classrooms") List<Classroom> classrooms);
    
    // Admin search methods
    @Query("SELECT t FROM Test t WHERE " +
           "(:subject IS NULL OR t.subject = :subject) " +
           "AND (:grade IS NULL OR t.grade = :grade)")
    List<Test> searchTestsAdmin(@Param("subject") String subject,
                                @Param("grade") String grade);
}
