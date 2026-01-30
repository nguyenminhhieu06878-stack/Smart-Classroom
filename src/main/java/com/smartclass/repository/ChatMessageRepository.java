package com.smartclass.repository;

import com.smartclass.model.ChatConversation;
import com.smartclass.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationOrderBySentAtAsc(ChatConversation conversation);

    List<ChatMessage> findByConversationOrderBySentAtDesc(ChatConversation conversation);
}
