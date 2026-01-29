package com.smartclass.repository;

import com.smartclass.model.StudentTest;
import com.smartclass.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentTestRepository extends JpaRepository<StudentTest, Long> {
    List<StudentTest> findByStudent(User student);
    List<StudentTest> findByStudentOrderByAssignedAtDesc(User student);
    Optional<StudentTest> findByStudentAndTestId(User student, Long testId);
}
