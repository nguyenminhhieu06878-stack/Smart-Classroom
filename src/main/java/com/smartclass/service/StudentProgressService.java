package com.smartclass.service;

import com.smartclass.model.StudentLesson;
import com.smartclass.model.StudentTest;
import com.smartclass.model.User;
import com.smartclass.repository.StudentLessonRepository;
import com.smartclass.repository.StudentTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudentProgressService {
    private final StudentLessonRepository studentLessonRepository;
    private final StudentTestRepository studentTestRepository;

    public StudentProgressService(StudentLessonRepository studentLessonRepository,
            StudentTestRepository studentTestRepository) {
        this.studentLessonRepository = studentLessonRepository;
        this.studentTestRepository = studentTestRepository;
    }

    public List<StudentLesson> getStudentLessons(User student) {
        return studentLessonRepository.findByStudent(student);
    }

    public List<StudentTest> getStudentTests(User student) {
        return studentTestRepository.findByStudent(student);
    }

    public Optional<StudentTest> getStudentTest(User student, Long testId) {
        return studentTestRepository.findByStudentAndTestId(student, testId);
    }

    @Transactional
    public StudentLesson saveStudentLesson(StudentLesson studentLesson) {
        return studentLessonRepository.save(studentLesson);
    }

    @Transactional
    public StudentTest saveStudentTest(StudentTest studentTest) {
        return studentTestRepository.save(studentTest);
    }
}
