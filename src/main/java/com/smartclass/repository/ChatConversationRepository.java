package com.smartclass.repository;

import com.smartclass.model.ChatConversation;
import com.smartclass.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByTeacherAndStudent(User teacher, User student);

    List<ChatConversation> findByTeacherOrderByLastMessageAtDesc(User teacher);

    List<ChatConversation> findByStudentOrderByLastMessageAtDesc(User student);

    @Query("SELECT c FROM ChatConversation c WHERE (c.teacher = :user OR c.student = :user) ORDER BY c.lastMessageAt DESC")
    List<ChatConversation> findByUserOrderByLastMessageAtDesc(User user);
}
