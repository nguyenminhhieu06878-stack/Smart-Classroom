package com.smartclass.service;

import com.smartclass.model.Test;
import com.smartclass.model.User;
import com.smartclass.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepository testRepository;
    
    public List<Test> getAllTests() {
        return testRepository.findAll();
    }
    
    public List<Test> getTestsByTeacher(User teacher) {
        return testRepository.findByTeacher(teacher);
    }
    
    public Optional<Test> getTestById(Long id) {
        return testRepository.findById(id);
    }
    
    @Transactional
    public Test createTest(Test test) {
        test.setCreatedAt(LocalDateTime.now());
        test.setUpdatedAt(LocalDateTime.now());
        return testRepository.save(test);
    }
    
    @Transactional
    public Test updateTest(Long id, Test testDetails) {
        Test test = testRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));
        
        test.setTitle(testDetails.getTitle());
        test.setSubject(testDetails.getSubject());
        test.setGrade(testDetails.getGrade());
        test.setTestMatrix(testDetails.getTestMatrix());
        test.setTotalPoints(testDetails.getTotalPoints());
        test.setDurationMinutes(testDetails.getDurationMinutes());
        test.setUpdatedAt(LocalDateTime.now());
        
        return testRepository.save(test);
    }
    
    @Transactional
    public void deleteTest(Long id) {
        testRepository.deleteById(id);
    }
    
    public long countTestsByTeacher(User teacher) {
        return testRepository.countByTeacher(teacher);
    }
}
