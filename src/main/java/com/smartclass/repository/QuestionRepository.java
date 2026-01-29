package com.smartclass.repository;

import com.smartclass.model.Question;
import com.smartclass.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTeacher(User teacher);
    Page<Question> findByTeacher(User teacher, Pageable pageable);
    List<Question> findByTeacherAndSubject(User teacher, String subject);
    List<Question> findByTeacherAndDifficulty(User teacher, Question.Difficulty difficulty);
    
    @Query("SELECT q FROM Question q WHERE q.teacher = :teacher " +
           "AND (:subject IS NULL OR q.subject = :subject) " +
           "AND (:grade IS NULL OR q.grade = :grade) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:keyword IS NULL OR q.topic LIKE %:keyword% OR q.content LIKE %:keyword%)")
    List<Question> searchQuestions(@Param("teacher") User teacher,
                                   @Param("subject") String subject,
                                   @Param("grade") String grade,
                                   @Param("difficulty") Question.Difficulty difficulty,
                                   @Param("keyword") String keyword);
    
    @Query("SELECT q FROM Question q WHERE q.teacher = :teacher " +
           "AND (:subject IS NULL OR q.subject = :subject) " +
           "AND (:grade IS NULL OR q.grade = :grade) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "AND (:keyword IS NULL OR q.topic LIKE %:keyword% OR q.content LIKE %:keyword%)")
    Page<Question> searchQuestions(@Param("teacher") User teacher,
                                   @Param("subject") String subject,
                                   @Param("grade") String grade,
                                   @Param("difficulty") Question.Difficulty difficulty,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);
    
    long countByTeacher(User teacher);
}
