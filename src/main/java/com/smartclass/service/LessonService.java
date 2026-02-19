package com.smartclass.service;

import com.smartclass.model.Classroom;
import com.smartclass.model.Lesson;
import com.smartclass.model.User;
import com.smartclass.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    public List<Lesson> getLessonsByTeacher(User teacher) {
        return lessonRepository.findByTeacher(teacher);
    }

    public List<Lesson> getLessonsForStudent(List<Classroom> classrooms) {
        return lessonRepository.findByClassroomsOrNoClassroom(classrooms);
    }

    public Optional<Lesson> getLessonById(Long id) {
        return lessonRepository.findById(id);
    }

    @Transactional
    public Lesson createLesson(Lesson lesson, Classroom classroom) {
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());
        lesson.setClassroom(classroom); // Set managed classroom entity
        return lessonRepository.save(lesson);
    }

    @Transactional
    public Lesson updateLesson(Long id, Lesson lessonDetails, Classroom classroom) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài giảng"));

        lesson.setTitle(lessonDetails.getTitle());
        lesson.setContent(lessonDetails.getContent());
        lesson.setSubject(lessonDetails.getSubject());
        lesson.setGrade(lessonDetails.getGrade());
        lesson.setTopic(lessonDetails.getTopic());
        lesson.setLearningObjectives(lessonDetails.getLearningObjectives());
        lesson.setVideoUrl(lessonDetails.getVideoUrl());
        lesson.setClassroom(classroom); // Set managed classroom entity
        lesson.setUpdatedAt(LocalDateTime.now());

        return lessonRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    public long countLessonsByTeacher(User teacher) {
        return lessonRepository.countByTeacher(teacher);
    }
}
