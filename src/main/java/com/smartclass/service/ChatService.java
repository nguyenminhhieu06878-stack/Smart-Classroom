package com.smartclass.service;

import com.smartclass.model.ChatConversation;
import com.smartclass.model.ChatMessage;
import com.smartclass.model.User;
import com.smartclass.repository.ChatConversationRepository;
import com.smartclass.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ChatConversation getOrCreateConversation(User teacher, User student) {
        Optional<ChatConversation> existing = conversationRepository.findByTeacherAndStudent(teacher, student);

        if (existing.isPresent()) {
            return existing.get();
        }

        ChatConversation conversation = new ChatConversation(teacher, student);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public ChatMessage sendMessage(Long conversationId, User sender, String content) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        ChatMessage message = new ChatMessage(conversation, sender, content);
        message = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return message;
    }

    public List<ChatMessage> getMessages(Long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        return messageRepository.findByConversationOrderBySentAtAsc(conversation);
    }

    public List<ChatConversation> getConversationsForUser(User user) {
        return conversationRepository.findByUserOrderByLastMessageAtDesc(user);
    }

    public Optional<ChatConversation> getConversationById(Long id) {
        return conversationRepository.findById(id);
    }

    @Transactional
    public void markMessagesAsRead(Long conversationId, User reader) {
        List<ChatMessage> messages = getMessages(conversationId);

        for (ChatMessage message : messages) {
            if (!message.getSender().getId().equals(reader.getId()) && !message.getIsRead()) {
                message.setIsRead(true);
                messageRepository.save(message);
            }
        }
    }
}
