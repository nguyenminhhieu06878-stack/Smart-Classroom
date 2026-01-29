package com.smartclass.service;

import com.smartclass.model.Classroom;
import com.smartclass.model.User;
import com.smartclass.repository.ClassroomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClassroomService {
    private final ClassroomRepository classroomRepository;

    public ClassroomService(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public long countByTeacher(User teacher) {
        return classroomRepository.countByTeacher(teacher);
    }

    public List<Classroom> findByTeacherAndIsActive(User teacher, boolean isActive) {
        return classroomRepository.findByTeacherAndIsActive(teacher, isActive);
    }

    public Optional<Classroom> getClassroomById(Long id) {
        return classroomRepository.findById(id);
    }

    public Optional<Classroom> getClassroomByRoomId(String roomId) {
        return classroomRepository.findByRoomId(roomId);
    }

    public boolean existsByRoomId(String roomId) {
        return classroomRepository.existsByRoomId(roomId);
    }

    @Transactional
    public Classroom saveClassroom(Classroom classroom) {
        if (classroom.getCreatedAt() == null) {
            classroom.setCreatedAt(LocalDateTime.now());
        }
        classroom.setUpdatedAt(LocalDateTime.now());
        return classroomRepository.save(classroom);
    }

    @Transactional
    public void deleteClassroom(Long id) {
        classroomRepository.deleteById(id);
    }
}
