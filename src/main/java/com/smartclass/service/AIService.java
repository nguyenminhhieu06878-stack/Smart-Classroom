package com.smartclass.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartclass.model.AIHistory;
import com.smartclass.model.Question;
import com.smartclass.model.User;
import com.smartclass.repository.AIHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AIService {
    
    private final AIHistoryRepository aiHistoryRepository;
    
    @Value("${ai.provider:groq}")
    private String aiProvider; // groq hoặc openai
    
    @Value("${groq.api.key:}")
    private String groqApiKey;
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;
    
    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;
    
    @Value("${openai.model:gpt-4}")
    private String openaiModel;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Tạo nội dung bài giảng với AI
     */
    public String generateLesson(String subject, String grade, String topic, String objectives, User teacher) {
        // Xác định ngôn ngữ dựa trên môn học
        boolean isEnglish = "Tiếng Anh".equalsIgnoreCase(subject);
        
        String prompt;
        if (isEnglish) {
            // Prompt tiếng Anh cho môn Tiếng Anh
            prompt = String.format(
                "You are a professional elementary school English teacher. Create a detailed lesson plan in ENGLISH for:\n" +
                "- Subject: English\n" +
                "- Grade: %s\n" +
                "- Topic: %s\n" +
                "- Objectives: %s\n\n" +
                "IMPORTANT: \n" +
                "1. The entire lesson plan must be in ENGLISH.\n" +
                "2. Do NOT use markdown formatting like **, ##, or any special characters. Write in plain text only.\n\n" +
                "The lesson plan should include:\n" +
                "1. Learning Objectives\n" +
                "2. Detailed Content\n" +
                "3. Classroom Activities\n" +
                "4. Assessment Questions\n" +
                "5. Homework\n\n" +
                "Write in plain text format without **, ##, or special formatting characters.",
                grade, topic, objectives
            );
        } else {
            // Prompt tiếng Việt cho các môn khác
            prompt = String.format(
                "Bạn là giáo viên tiểu học chuyên nghiệp. Hãy soạn giáo án chi tiết cho:\n" +
                "- Môn học: %s\n" +
                "- Lớp: %s\n" +
                "- Chủ đề: %s\n" +
                "- Mục tiêu: %s\n\n" +
                "QUAN TRỌNG: Không sử dụng ký tự markdown như **, ##, hoặc bất kỳ định dạng đặc biệt nào. Chỉ viết văn bản thuần túy.\n\n" +
                "Giáo án cần bao gồm:\n" +
                "1. Mục tiêu bài học\n" +
                "2. Nội dung chi tiết\n" +
                "3. Hoạt động lớp học\n" +
                "4. Câu hỏi kiểm tra\n" +
                "5. Bài tập về nhà\n\n" +
                "Viết theo định dạng văn bản thông thường, không dùng **, ##, hoặc các ký tự đặc biệt.",
                subject, grade, topic, objectives
            );
        }
        
        String response = callAI(prompt);
        
        // Remove markdown formatting characters as a safety measure
        response = response.replaceAll("\\*\\*", "")      // Remove bold **
                          .replaceAll("###\\s+", "")      // Remove ### headers
                          .replaceAll("##\\s+", "")       // Remove ## headers  
                          .replaceAll("#\\s+", "")        // Remove # headers
                          .replaceAll("\\*", "")          // Remove single asterisks
                          .replaceAll("__", "")           // Remove underscores for bold
                          .replaceAll("_", "");           // Remove single underscores for italic
        
        // Lưu lịch sử
        saveHistory(teacher, AIHistory.AIType.LESSON, prompt, response, subject, grade, topic, 1);
        
        return response;
    }
    
    /**
     * Tạo câu hỏi hàng loạt với AI
     */
    public List<Map<String, Object>> generateQuestions(String subject, String grade, 
                                                       String topic, int easyCount, 
                                                       int mediumCount, int hardCount, User teacher) {
        // Xác định ngôn ngữ dựa trên môn học
        boolean isEnglish = "Tiếng Anh".equalsIgnoreCase(subject);
        
        String prompt;
        if (isEnglish) {
            // Prompt tiếng Anh cho môn Tiếng Anh
            prompt = String.format(
                "Create %d MULTIPLE CHOICE questions in ENGLISH for elementary school students:\n" +
                "- Subject: English, Grade: %s, Topic: %s\n" +
                "- %d easy questions, %d medium questions, %d hard questions\n\n" +
                "IMPORTANT: All questions, options, and explanations must be in ENGLISH.\n\n" +
                "Return JSON array with this format:\n" +
                "[{\n" +
                "  \"content\": \"Question content in English\",\n" +
                "  \"optionA\": \"Option A in English\",\n" +
                "  \"optionB\": \"Option B in English\",\n" +
                "  \"optionC\": \"Option C in English\",\n" +
                "  \"optionD\": \"Option D in English\",\n" +
                "  \"correctAnswer\": \"A\",\n" +
                "  \"explanation\": \"Explanation in English\",\n" +
                "  \"difficulty\": \"EASY|MEDIUM|HARD\",\n" +
                "  \"points\": 1.0\n" +
                "}]",
                easyCount + mediumCount + hardCount, grade, topic,
                easyCount, mediumCount, hardCount
            );
        } else {
            // Prompt tiếng Việt cho các môn khác
            prompt = String.format(
                "Tạo %d câu hỏi TRẮC NGHIỆM cho học sinh tiểu học:\n" +
                "- Môn: %s, Lớp: %s, Chủ đề: %s\n" +
                "- %d câu dễ, %d câu trung bình, %d câu khó\n\n" +
                "Trả về JSON array với format:\n" +
                "[{\n" +
                "  \"content\": \"Nội dung câu hỏi\",\n" +
                "  \"optionA\": \"Đáp án A\",\n" +
                "  \"optionB\": \"Đáp án B\",\n" +
                "  \"optionC\": \"Đáp án C\",\n" +
                "  \"optionD\": \"Đáp án D\",\n" +
                "  \"correctAnswer\": \"A\",\n" +
                "  \"explanation\": \"Giải thích\",\n" +
                "  \"difficulty\": \"EASY|MEDIUM|HARD\",\n" +
                "  \"points\": 1.0\n" +
                "}]",
                easyCount + mediumCount + hardCount, subject, grade, topic,
                easyCount, mediumCount, hardCount
            );
        }
        
        String response = callAI(prompt);
        List<Map<String, Object>> questions = parseQuestionsFromResponse(response);
        
        // Lưu lịch sử
        saveHistory(teacher, AIHistory.AIType.QUESTIONS, prompt, response, subject, grade, topic, questions.size());
        
        return questions;
    }
    
    /**
     * Tạo ma trận đề thi
     */
    public Map<String, Object> generateTestMatrix(String subject, String grade, 
                                                  List<String> topics, int totalQuestions) {
        String prompt = String.format(
            "Tạo ma trận đề thi cho:\n" +
            "- Môn: %s, Lớp: %s\n" +
            "- Chủ đề: %s\n" +
            "- Tổng số câu: %d\n\n" +
            "Phân bổ câu hỏi theo mức độ (40%% dễ, 40%% TB, 20%% khó)\n" +
            "Trả về JSON với format:\n" +
            "{\n" +
            "  \"matrix\": [\n" +
            "    {\"topic\": \"Chủ đề\", \"easy\": 2, \"medium\": 2, \"hard\": 1}\n" +
            "  ],\n" +
            "  \"totalPoints\": 10.0\n" +
            "}",
            subject, grade, String.join(", ", topics), totalQuestions
        );
        
        String response = callAI(prompt);
        return parseMatrixFromResponse(response);
    }
    
    /**
     * Gọi AI API (Groq hoặc OpenAI)
     */
    private String callAI(String prompt) {
        if ("groq".equalsIgnoreCase(aiProvider)) {
            return callGroq(prompt);
        } else {
            return callOpenAI(prompt);
        }
    }
    
    /**
     * Gọi Groq API (nhanh và miễn phí)
     */
    private String callGroq(String prompt) {
        try {
            String url = "https://api.groq.com/openai/v1/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", groqModel);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi Groq API: " + e.getMessage());
        }
    }
    
    /**
     * Gọi OpenAI API
     */
    private String callOpenAI(String prompt) {
        try {
            String url = "https://api.openai.com/v1/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openaiModel);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi OpenAI API: " + e.getMessage());
        }
    }
    
    private List<Map<String, Object>> parseQuestionsFromResponse(String response) {
        try {
            // Extract JSON from response
            int start = response.indexOf("[");
            int end = response.lastIndexOf("]") + 1;
            if (start >= 0 && end > start) {
                String json = response.substring(start, end);
                return objectMapper.readValue(json, List.class);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private Map<String, Object> parseMatrixFromResponse(String response) {
        try {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                String json = response.substring(start, end);
                return objectMapper.readValue(json, Map.class);
            }
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    /**
     * Lưu lịch sử AI
     */
    private void saveHistory(User teacher, AIHistory.AIType aiType, String prompt, 
                           String response, String subject, String grade, String topic, int itemsCount) {
        try {
            AIHistory history = new AIHistory();
            history.setTeacher(teacher);
            history.setAiType(aiType);
            history.setPrompt(prompt);
            history.setResponse(response);
            history.setSubject(subject);
            history.setGrade(grade);
            history.setTopic(topic);
            history.setItemsCount(itemsCount);
            aiHistoryRepository.save(history);
        } catch (Exception e) {
            // Log error nhưng không throw để không ảnh hưởng chức năng chính
            System.err.println("Lỗi khi lưu lịch sử AI: " + e.getMessage());
        }
    }
}
