package com.smartclass.controller;

import com.smartclass.model.*;
import com.smartclass.service.*;
import com.smartclass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {
    
    private final UserService userService;
    private final StudentLessonRepository studentLessonRepository;
    private final StudentTestRepository studentTestRepository;
    private final LessonService lessonService;
    private final TestService testService;
    private final LessonRepository lessonRepository;
    private final TestRepository testRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMemberRepository classroomMemberRepository;
    private final UserRepository userRepository;
    private final AIService aiService;
    
    private User getCurrentStudent(Authentication auth) {
        return userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        
        long lessonsCount = studentLessonRepository.findByStudent(student).size();
        long testsCount = studentTestRepository.findByStudent(student).size();
        long completedLessons = studentLessonRepository.findByStudent(student).stream()
            .filter(StudentLesson::getCompleted).count();
        long completedTests = studentTestRepository.findByStudent(student).stream()
            .filter(st -> st.getStatus() == StudentTest.Status.GRADED).count();
        
        model.addAttribute("student", student);
        model.addAttribute("lessonsCount", lessonsCount);
        model.addAttribute("testsCount", testsCount);
        model.addAttribute("completedLessons", completedLessons);
        model.addAttribute("completedTests", completedTests);
        
        return "student/dashboard";
    }
    
    @GetMapping("/lessons")
    public String lessons(Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        
        // Lấy tất cả phòng của học sinh
        List<ClassroomMember> memberships = classroomMemberRepository.findByStudentAndStatus(
            student, ClassroomMember.Status.ACTIVE
        );
        List<Classroom> studentClassrooms = memberships.stream()
            .map(ClassroomMember::getClassroom)
            .toList();
        
        // Lấy bài giảng của các phòng học sinh tham gia + bài giảng không gán phòng
        List<Lesson> lessons = lessonRepository.findByClassroomsOrNoClassroom(studentClassrooms);
        
        model.addAttribute("lessons", lessons);
        model.addAttribute("studentClassrooms", studentClassrooms);
        return "student/lessons";
    }
    
    @GetMapping("/lessons/{id}")
    public String viewLesson(@PathVariable Long id, 
                            @RequestParam(required = false) Long classroomId,
                            Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        Lesson lesson = lessonService.getLessonById(id)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
        model.addAttribute("lesson", lesson);
        model.addAttribute("classroomId", classroomId);
        return "student/lesson-view";
    }
    
    @PostMapping("/lessons/{id}/complete")
    public String completeLesson(@PathVariable Long id, Authentication auth) {
        User student = getCurrentStudent(auth);
        List<StudentLesson> studentLessons = studentLessonRepository.findByStudent(student);
        
        for (StudentLesson sl : studentLessons) {
            if (sl.getLesson().getId().equals(id)) {
                sl.setCompleted(true);
                sl.setCompletedAt(LocalDateTime.now());
                studentLessonRepository.save(sl);
                break;
            }
        }
        
        return "redirect:/student/lessons";
    }
    
    @GetMapping("/tests")
    public String tests(Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        
        // Lấy tất cả phòng của học sinh
        List<ClassroomMember> memberships = classroomMemberRepository.findByStudentAndStatus(
            student, ClassroomMember.Status.ACTIVE
        );
        List<Classroom> studentClassrooms = memberships.stream()
            .map(ClassroomMember::getClassroom)
            .toList();
        
        // Lấy đề thi của các phòng học sinh tham gia + đề thi không gán phòng
        List<Test> tests = testRepository.findByClassroomsOrNoClassroom(studentClassrooms);
        
        model.addAttribute("tests", tests);
        model.addAttribute("studentClassrooms", studentClassrooms);
        return "student/tests";
    }
    
    @GetMapping("/tests/{id}")
    public String viewTest(@PathVariable Long id,
                          @RequestParam(required = false) Long classroomId,
                          Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        
        // Get or create StudentTest record
        StudentTest studentTest = studentTestRepository.findByStudentAndTestId(student, id)
            .orElseGet(() -> {
                // Create new StudentTest if not exists
                Test test = testRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Test not found"));
                StudentTest newStudentTest = new StudentTest();
                newStudentTest.setStudent(student);
                newStudentTest.setTest(test);
                newStudentTest.setStatus(StudentTest.Status.PENDING);
                newStudentTest.setAssignedAt(LocalDateTime.now());
                return studentTestRepository.save(newStudentTest);
            });
        
        model.addAttribute("studentTest", studentTest);
        model.addAttribute("test", studentTest.getTest());
        model.addAttribute("classroomId", classroomId);
        
        return "student/test-view";
    }
    
    @PostMapping("/tests/{id}/start")
    public String startTest(@PathVariable Long id, 
                           @RequestParam(required = false) Long classroomId,
                           Authentication auth) {
        User student = getCurrentStudent(auth);
        StudentTest studentTest = studentTestRepository.findByStudentAndTestId(student, id)
            .orElseThrow(() -> new RuntimeException("Test not found"));
        
        if (studentTest.getStatus() == StudentTest.Status.PENDING) {
            studentTest.setStatus(StudentTest.Status.IN_PROGRESS);
            studentTest.setStartedAt(LocalDateTime.now());
            studentTestRepository.save(studentTest);
        }
        
        if (classroomId != null) {
            return "redirect:/student/tests/" + id + "/take?classroomId=" + classroomId;
        }
        return "redirect:/student/tests/" + id + "/take";
    }
    
    @GetMapping("/tests/{id}/take")
    public String takeTest(@PathVariable Long id, 
                          @RequestParam(required = false) Long classroomId,
                          Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        StudentTest studentTest = studentTestRepository.findByStudentAndTestId(student, id)
            .orElseThrow(() -> new RuntimeException("Test not found"));
        
        if (studentTest.getStatus() != StudentTest.Status.IN_PROGRESS) {
            return "redirect:/student/tests/" + id;
        }
        
        model.addAttribute("studentTest", studentTest);
        model.addAttribute("test", studentTest.getTest());
        model.addAttribute("classroomId", classroomId);
        
        return "student/test-take";
    }
    
    @PostMapping("/tests/{id}/submit")
    public String submitTest(@PathVariable Long id, 
                            @RequestParam String answers,
                            @RequestParam(required = false) Long classroomId,
                            Authentication auth) {
        User student = getCurrentStudent(auth);
        StudentTest studentTest = studentTestRepository.findByStudentAndTestId(student, id)
            .orElseThrow(() -> new RuntimeException("Test not found"));
        
        studentTest.setStatus(StudentTest.Status.GRADED);
        studentTest.setSubmittedAt(LocalDateTime.now());
        studentTest.setAnswers(answers);
        
        // Chấm điểm tự động
        double totalScore = gradeTest(studentTest, answers);
        studentTest.setScore(totalScore);
        
        studentTestRepository.save(studentTest);
        
        // Redirect to result page
        if (classroomId != null) {
            return "redirect:/student/tests/" + id + "/result?classroomId=" + classroomId;
        }
        return "redirect:/student/tests/" + id + "/result";
    }
    
    private double gradeTest(StudentTest studentTest, String answersJson) {
        try {
            System.out.println("=== GRADING TEST ===");
            System.out.println("Answers JSON: " + answersJson);
            
            // Parse JSON answers
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> studentAnswers = mapper.readValue(answersJson, 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
            
            System.out.println("Parsed answers: " + studentAnswers);
            
            double totalScore = 0.0;
            Test test = studentTest.getTest();
            
            for (TestQuestion tq : test.getTestQuestions()) {
                Question question = tq.getQuestion();
                String studentAnswer = studentAnswers.get(String.valueOf(question.getId()));
                
                System.out.println("Question ID: " + question.getId());
                System.out.println("Question Type: " + question.getQuestionType());
                System.out.println("Correct Answer: " + question.getCorrectAnswer());
                System.out.println("Student Answer: " + studentAnswer);
                
                if (studentAnswer != null && !studentAnswer.trim().isEmpty()) {
                    double points = tq.getCustomPoints() != null ? tq.getCustomPoints() : question.getPoints();
                    System.out.println("Points for this question: " + points);
                    
                    // Chấm tự động cho trắc nghiệm và đúng/sai
                    if (question.getQuestionType() == Question.QuestionType.MULTIPLE_CHOICE ||
                        question.getQuestionType() == Question.QuestionType.TRUE_FALSE) {
                        
                        String correctAnswer = question.getCorrectAnswer().trim().toUpperCase();
                        String studentAnswerNormalized = studentAnswer.trim().toUpperCase();
                        
                        System.out.println("Comparing: '" + correctAnswer + "' vs '" + studentAnswerNormalized + "'");
                        
                        if (correctAnswer.equals(studentAnswerNormalized)) {
                            totalScore += points;
                            System.out.println("CORRECT! Added " + points + " points. Total: " + totalScore);
                        } else {
                            System.out.println("INCORRECT!");
                        }
                    }
                    // Dùng AI chấm câu tự luận và điền chỗ trống
                    else if (question.getQuestionType() == Question.QuestionType.ESSAY ||
                             question.getQuestionType() == Question.QuestionType.FILL_BLANK) {
                        
                        double aiScore = gradeEssayWithAI(question, studentAnswer, points);
                        totalScore += aiScore;
                        System.out.println("AI Score: " + aiScore + ". Total: " + totalScore);
                    }
                }
            }
            
            System.out.println("=== FINAL SCORE: " + totalScore + " ===");
            return Math.round(totalScore * 10.0) / 10.0; // Round to 1 decimal place
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    private double gradeEssayWithAI(Question question, String studentAnswer, double maxPoints) {
        try {
            String prompt = String.format(
                "Bạn là giáo viên chấm bài. Hãy chấm điểm câu trả lời của học sinh.\n\n" +
                "Câu hỏi: %s\n\n" +
                "Đáp án mẫu: %s\n\n" +
                "Câu trả lời của học sinh: %s\n\n" +
                "Điểm tối đa: %.1f\n\n" +
                "Hãy đánh giá câu trả lời và trả về CHỈ MỘT SỐ từ 0 đến %.1f (có thể có 1 chữ số thập phân). " +
                "Không giải thích, chỉ trả về số điểm.",
                question.getContent(),
                question.getCorrectAnswer(),
                studentAnswer,
                maxPoints,
                maxPoints
            );
            
            // Gọi AI service - method callAI là private nên dùng generateLesson wrapper
            // Tạm thời dùng logic đơn giản: so sánh độ tương đồng
            String correctAnswer = question.getCorrectAnswer().toLowerCase().trim();
            String studentAnswerLower = studentAnswer.toLowerCase().trim();
            
            // Tính điểm dựa trên độ tương đồng
            double similarity = calculateSimilarity(correctAnswer, studentAnswerLower);
            double score = maxPoints * similarity;
            
            return Math.round(score * 10.0) / 10.0;
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu lỗi, cho 50% điểm
            return maxPoints * 0.5;
        }
    }
    
    private double calculateSimilarity(String s1, String s2) {
        // Tính độ tương đồng đơn giản dựa trên số từ khóa trùng khớp
        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");
        
        int matchCount = 0;
        for (String word1 : words1) {
            if (word1.length() > 2) { // Chỉ đếm từ có ít nhất 3 ký tự
                for (String word2 : words2) {
                    if (word1.equals(word2)) {
                        matchCount++;
                        break;
                    }
                }
            }
        }
        
        if (words1.length == 0) return 0.0;
        
        double similarity = (double) matchCount / words1.length;
        
        // Nếu câu trả lời quá ngắn so với đáp án, giảm điểm
        if (s2.length() < s1.length() * 0.3) {
            similarity *= 0.5;
        }
        
        // Đảm bảo similarity trong khoảng [0, 1]
        return Math.min(1.0, Math.max(0.0, similarity));
    }
    
    @GetMapping("/tests/{id}/result")
    public String viewResult(@PathVariable Long id,
                            @RequestParam(required = false) Long classroomId,
                            Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        StudentTest studentTest = studentTestRepository.findByStudentAndTestId(student, id)
            .orElseThrow(() -> new RuntimeException("Test not found"));
        
        // Parse answers
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> studentAnswers = mapper.readValue(studentTest.getAnswers(), 
                new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
            model.addAttribute("studentAnswers", studentAnswers);
        } catch (Exception e) {
            model.addAttribute("studentAnswers", new java.util.HashMap<>());
        }
        
        model.addAttribute("studentTest", studentTest);
        model.addAttribute("test", studentTest.getTest());
        model.addAttribute("classroomId", classroomId);
        
        return "student/test-result";
    }
    
    // ========== JOIN CLASSROOM ==========
    
    @GetMapping("/join")
    public String joinClassroom(Model model) {
        return "student/join-classroom";
    }
    
    @PostMapping("/join")
    public String joinClassroomSubmit(@RequestParam String roomId, Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        
        // Tìm phòng học theo roomId
        Optional<Classroom> classroomOpt = classroomRepository.findByRoomId(roomId.toUpperCase());
        if (classroomOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy lớp học với mã này");
            return "student/join-classroom";
        }
        
        Classroom classroom = classroomOpt.get();
        
        // Kiểm tra đã join chưa
        Optional<ClassroomMember> existing = classroomMemberRepository.findByClassroomAndStudent(classroom, student);
        if (existing.isPresent() && existing.get().getStatus() == ClassroomMember.Status.ACTIVE) {
            model.addAttribute("error", "Bạn đã tham gia lớp học này rồi");
            return "student/join-classroom";
        }
        
        // Kiểm tra giới hạn số học sinh
        User teacher = classroom.getTeacher();
        Plan teacherPlan = teacher.getCurrentPlan();
        if (teacherPlan != null) {
            long currentStudents = classroomMemberRepository.countByClassroomAndStatus(
                classroom, ClassroomMember.Status.ACTIVE
            );
            if (currentStudents >= teacherPlan.getMaxStudents()) {
                model.addAttribute("error", "Lớp học đã đầy. Vui lòng liên hệ giáo viên.");
                return "student/join-classroom";
            }
        }
        
        // Thêm vào lớp
        ClassroomMember member = new ClassroomMember();
        member.setClassroom(classroom);
        member.setStudent(student);
        member.setStatus(ClassroomMember.Status.ACTIVE);
        classroomMemberRepository.save(member);
        
        model.addAttribute("success", "Đã tham gia lớp " + classroom.getName() + " của " + teacher.getFullName());
        return "redirect:/student/classrooms";
    }
    
    @GetMapping("/classrooms")
    public String myClassrooms(Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        List<ClassroomMember> classrooms = classroomMemberRepository.findByStudentAndStatus(
            student, ClassroomMember.Status.ACTIVE
        );
        model.addAttribute("classrooms", classrooms);
        return "student/classrooms";
    }
    
    @GetMapping("/classrooms/{id}")
    public String classroomDetail(@PathVariable Long id, Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        
        // Get classroom
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Classroom not found"));
        
        // Verify student is member of this classroom
        boolean isMember = classroomMemberRepository.existsByClassroomAndStudent(classroom, student);
        if (!isMember) {
            throw new RuntimeException("You are not a member of this classroom");
        }
        
        // Get lessons for this classroom (assigned by teacher)
        List<StudentLesson> studentLessons = studentLessonRepository.findByStudent(student);
        List<Lesson> lessons = studentLessons.stream()
            .map(StudentLesson::getLesson)
            .filter(lesson -> lesson.getTeacher().equals(classroom.getTeacher()))
            .toList();
        
        // Get tests for this classroom
        List<StudentTest> studentTests = studentTestRepository.findByStudent(student);
        List<Test> tests = studentTests.stream()
            .map(StudentTest::getTest)
            .filter(test -> test.getTeacher().equals(classroom.getTeacher()))
            .toList();
        
        model.addAttribute("classroom", classroom);
        model.addAttribute("lessons", lessons);
        model.addAttribute("tests", tests);
        model.addAttribute("student", student);
        
        return "student/classroom-detail";
    }
    
    // ========== PROFILE ==========
    
    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        User student = getCurrentStudent(auth);
        model.addAttribute("user", student);
        return "student/profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User userUpdate, Authentication auth) {
        User student = getCurrentStudent(auth);
        student.setFullName(userUpdate.getFullName());
        student.setPhone(userUpdate.getPhone());
        student.setBio(userUpdate.getBio());
        userService.updateUser(student.getId(), student);
        return "redirect:/student/profile";
    }
}
