package com.smartclass.repository;

import com.smartclass.model.AIHistory;
import com.smartclass.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AIHistoryRepository extends JpaRepository<AIHistory, Long> {
    List<AIHistory> findByTeacherOrderByCreatedAtDesc(User teacher);
    List<AIHistory> findByTeacherAndAiTypeOrderByCreatedAtDesc(User teacher, AIHistory.AIType aiType);
    
    // Admin methods
    List<AIHistory> findByAiTypeOrderByCreatedAtDesc(AIHistory.AIType aiType);
    
    @Query("SELECT ah FROM AIHistory ah ORDER BY ah.createdAt DESC")
    List<AIHistory> findAllOrderByCreatedAtDesc();
    
    long countByTeacher(User teacher);
}
