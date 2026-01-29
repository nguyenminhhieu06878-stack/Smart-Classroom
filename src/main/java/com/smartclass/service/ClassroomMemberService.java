package com.smartclass.service;

import com.smartclass.model.Classroom;
import com.smartclass.model.ClassroomMember;
import com.smartclass.model.User;
import com.smartclass.repository.ClassroomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClassroomMemberService {
    private final ClassroomMemberRepository classroomMemberRepository;

    public ClassroomMemberService(ClassroomMemberRepository classroomMemberRepository) {
        this.classroomMemberRepository = classroomMemberRepository;
    }

    public long countByClassroomAndStatus(Classroom classroom, ClassroomMember.Status status) {
        return classroomMemberRepository.countByClassroomAndStatus(classroom, status);
    }

    public List<ClassroomMember> findByClassroomAndStatus(Classroom classroom, ClassroomMember.Status status) {
        return classroomMemberRepository.findByClassroomAndStatus(classroom, status);
    }

    public List<ClassroomMember> getMembershipsByStudent(User student, ClassroomMember.Status status) {
        return classroomMemberRepository.findByStudentAndStatus(student, status);
    }

    public Optional<ClassroomMember> getMember(Classroom classroom, User student) {
        return classroomMemberRepository.findByClassroomAndStudent(classroom, student);
    }

    public boolean isMember(Classroom classroom, User student) {
        return classroomMemberRepository.existsByClassroomAndStudent(classroom, student);
    }

    @Transactional
    public ClassroomMember saveMember(ClassroomMember member) {
        if (member.getJoinedAt() == null) {
            member.setJoinedAt(LocalDateTime.now());
        }
        return classroomMemberRepository.save(member);
    }
}
