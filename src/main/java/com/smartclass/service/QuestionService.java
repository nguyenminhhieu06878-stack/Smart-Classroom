package com.smartclass.service;

import com.smartclass.model.Question;
import com.smartclass.model.User;
import com.smartclass.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
    
    public List<Question> getQuestionsByTeacher(User teacher) {
        return questionRepository.findByTeacher(teacher);
    }
    
    public Page<Question> getQuestionsByTeacher(User teacher, Pageable pageable) {
        return questionRepository.findByTeacher(teacher, pageable);
    }
    
    public Optional<Question> getQuestionById(Long id) {
        return questionRepository.findById(id);
    }
    
    public List<Question> searchQuestions(User teacher, String subject, String grade,
                                         Question.Difficulty difficulty, String keyword) {
        return questionRepository.searchQuestions(teacher, subject, grade, difficulty, keyword);
    }
    
    public Page<Question> searchQuestions(User teacher, String subject, String grade,
                                         Question.Difficulty difficulty, String keyword,
                                         Pageable pageable) {
        return questionRepository.searchQuestions(teacher, subject, grade, difficulty, keyword, pageable);
    }
    
    @Transactional
    public Question createQuestion(Question question) {
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        return questionRepository.save(question);
    }
    
    @Transactional
    public Question updateQuestion(Long id, Question questionDetails) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));
        
        question.setContent(questionDetails.getContent());
        question.setQuestionType(questionDetails.getQuestionType());
        question.setCorrectAnswer(questionDetails.getCorrectAnswer());
        question.setOptionA(questionDetails.getOptionA());
        question.setOptionB(questionDetails.getOptionB());
        question.setOptionC(questionDetails.getOptionC());
        question.setOptionD(questionDetails.getOptionD());
        question.setExplanation(questionDetails.getExplanation());
        question.setDifficulty(questionDetails.getDifficulty());
        question.setSubject(questionDetails.getSubject());
        question.setGrade(questionDetails.getGrade());
        question.setTopic(questionDetails.getTopic());
        question.setReferenceBook(questionDetails.getReferenceBook());
        question.setReferencePage(questionDetails.getReferencePage());
        question.setPoints(questionDetails.getPoints());
        question.setIsPublic(questionDetails.getIsPublic());
        question.setUpdatedAt(LocalDateTime.now());
        
        return questionRepository.save(question);
    }
    
    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
    
    public long countQuestionsByTeacher(User teacher) {
        return questionRepository.countByTeacher(teacher);
    }
    
    @Transactional
    public void incrementUsageCount(Long questionId) {
        Question question = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));
        question.setUsageCount(question.getUsageCount() + 1);
        questionRepository.save(question);
    }
}
