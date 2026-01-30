package com.smartclass.controller;

import com.smartclass.model.ChatConversation;
import com.smartclass.model.ChatMessage;
import com.smartclass.model.User;
import com.smartclass.service.ChatService;
import com.smartclass.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, UserService userService,
            SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Teacher chat page
    @GetMapping("/teacher/chat")
    public String teacherChat(Authentication authentication, Model model,
            @RequestParam(required = false) Long conversationId) {
        User teacher = getCurrentUser(authentication);

        List<ChatConversation> conversations = chatService.getConversationsForUser(teacher);
        List<User> students = userService.getAllStudents();

        model.addAttribute("conversations", conversations);
        model.addAttribute("students", students);
        model.addAttribute("currentUser", teacher);

        if (conversationId != null) {
            ChatConversation conversation = chatService.getConversationById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            List<ChatMessage> messages = chatService.getMessages(conversationId);
            chatService.markMessagesAsRead(conversationId, teacher);

            model.addAttribute("selectedConversation", conversation);
            model.addAttribute("messages", messages);
        }

        return "teacher/chat";
    }

    // Student chat page
    @GetMapping("/student/chat")
    public String studentChat(Authentication authentication, Model model,
            @RequestParam(required = false) Long conversationId) {
        User student = getCurrentUser(authentication);

        List<ChatConversation> conversations = chatService.getConversationsForUser(student);
        List<User> teachers = userService.getAllTeachers();

        model.addAttribute("conversations", conversations);
        model.addAttribute("teachers", teachers);
        model.addAttribute("currentUser", student);

        if (conversationId != null) {
            ChatConversation conversation = chatService.getConversationById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
            List<ChatMessage> messages = chatService.getMessages(conversationId);
            chatService.markMessagesAsRead(conversationId, student);

            model.addAttribute("selectedConversation", conversation);
            model.addAttribute("messages", messages);
        }

        return "student/chat";
    }

    // Create new conversation (MVC style)
    @PostMapping("/teacher/chat/new")
    public String teacherCreateConversation(
            @RequestParam Long studentId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User teacher = getCurrentUser(authentication);
        User student = userService.getUserById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        ChatConversation conversation = chatService.getOrCreateConversation(teacher, student);

        return "redirect:/teacher/chat?conversationId=" + conversation.getId();
    }

    @PostMapping("/student/chat/new")
    public String studentCreateConversation(
            @RequestParam Long teacherId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User student = getCurrentUser(authentication);
        User teacher = userService.getUserById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        ChatConversation conversation = chatService.getOrCreateConversation(teacher, student);

        return "redirect:/student/chat?conversationId=" + conversation.getId();
    }

    // WebSocket: Send message
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload, Authentication authentication) {
        try {
            Long conversationId = Long.valueOf(payload.get("conversationId").toString());
            String content = payload.get("content").toString();

            User sender = getCurrentUser(authentication);

            ChatMessage message = chatService.sendMessage(conversationId, sender, content);

            // Get conversation to determine recipient
            ChatConversation conversation = chatService.getConversationById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            // Prepare message data
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("id", message.getId());
            messageData.put("conversationId", conversationId);
            messageData.put("content", message.getContent());
            messageData.put("sentAt", message.getSentAt().toString());
            messageData.put("sender", Map.of(
                    "id", sender.getId(),
                    "name", sender.getFullName()));

            // Send to both participants
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + conversationId,
                    messageData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
