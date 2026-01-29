package com.smartclass.repository;

import com.smartclass.model.Classroom;
import com.smartclass.model.ClassroomMember;
import com.smartclass.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomMemberRepository extends JpaRepository<ClassroomMember, Long> {
    List<ClassroomMember> findByClassroomAndStatus(Classroom classroom, ClassroomMember.Status status);
    List<ClassroomMember> findByStudentAndStatus(User student, ClassroomMember.Status status);
    Optional<ClassroomMember> findByClassroomAndStudent(Classroom classroom, User student);
    boolean existsByClassroomAndStudent(Classroom classroom, User student);
    long countByClassroomAndStatus(Classroom classroom, ClassroomMember.Status status);
}
