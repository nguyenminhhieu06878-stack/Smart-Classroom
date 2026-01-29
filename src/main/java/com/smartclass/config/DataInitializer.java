package com.smartclass.config;

import com.smartclass.model.*;
import com.smartclass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final TestRepository testRepository;
    private final StudentLessonRepository studentLessonRepository;
    private final StudentTestRepository studentTestRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMemberRepository classroomMemberRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // Tạo Plans nếu chưa có
        if (planRepository.count() == 0) {
            createPlans();
        }
        
        // Tạo users mẫu nếu chưa có
        if (userRepository.count() == 0) {
            createSampleUsers();
        } else {
            // Nếu users đã tồn tại, xóa các subscriptions cũ của teacher mẫu
            cleanupOldSubscriptions();
        }
        
        // Tạo data mẫu cho teacher
        if (questionRepository.count() == 0) {
            createSampleData();
        }
    }
    
    private void cleanupOldSubscriptions() {
        User teacher = userRepository.findByEmail("teacher@smartclass.vn").orElse(null);
        if (teacher != null) {
            // Xóa tất cả subscriptions của teacher mẫu
            List<Subscription> oldSubscriptions = subscriptionRepository.findByUser(teacher);
            if (!oldSubscriptions.isEmpty()) {
                subscriptionRepository.deleteAll(oldSubscriptions);
                System.out.println("✅ Đã xóa " + oldSubscriptions.size() + " subscriptions cũ của teacher mẫu");
            }
            
            // Đảm bảo teacher không có currentPlan
            if (teacher.getCurrentPlan() != null) {
                teacher.setCurrentPlan(null);
                userRepository.save(teacher);
                System.out.println("✅ Đã reset currentPlan của teacher mẫu");
            }
        }
    }
    
    private void createPlans() {
        // Free Plan - Dùng thử
        Plan free = new Plan();
        free.setName("Miễn phí");
        free.setDescription("Dùng thử cho giáo viên cá nhân");
        free.setPrice(0.0);
        free.setMaxClasses(1);
        free.setMaxStudents(20);
        free.setAiLessonsPerMonth(3);
        free.setTestsPerMonth(2);
        free.setMaxQuestionsBank(30);
        free.setAutoGrading(false);
        free.setAdvancedReports(false);
        free.setActive(true);
        planRepository.save(free);
        
        // Basic Plan - Giáo viên cá nhân
        Plan basic = new Plan();
        basic.setName("Cơ bản");
        basic.setDescription("Cho giáo viên dạy 2-5 lớp");
        basic.setPrice(79000.0);
        basic.setMaxClasses(5);
        basic.setMaxStudents(40);
        basic.setAiLessonsPerMonth(20);
        basic.setTestsPerMonth(10);
        basic.setMaxQuestionsBank(200);
        basic.setAutoGrading(true);
        basic.setAdvancedReports(false);
        basic.setActive(true);
        planRepository.save(basic);
        
        // Pro Plan - Giáo viên chuyên nghiệp
        Plan pro = new Plan();
        pro.setName("Chuyên nghiệp");
        pro.setDescription("Cho giáo viên dạy nhiều lớp");
        pro.setPrice(149000.0);
        pro.setMaxClasses(20);
        pro.setMaxStudents(50);
        pro.setAiLessonsPerMonth(100);
        pro.setTestsPerMonth(50);
        pro.setMaxQuestionsBank(1000);
        pro.setAutoGrading(true);
        pro.setAdvancedReports(true);
        pro.setActive(true);
        planRepository.save(pro);
        
        // School Plan - Toàn trường
        Plan school = new Plan();
        school.setName("Trường học");
        school.setDescription("Giải pháp cho toàn trường học");
        school.setPrice(999000.0);
        school.setMaxClasses(999);
        school.setMaxStudents(999);
        school.setAiLessonsPerMonth(9999);
        school.setTestsPerMonth(9999);
        school.setMaxQuestionsBank(99999);
        school.setAutoGrading(true);
        school.setAdvancedReports(true);
        school.setActive(true);
        planRepository.save(school);
        
        System.out.println("✅ Đã tạo 4 gói dịch vụ");
    }
    
    private void createSampleUsers() {
        // Admin
        User admin = new User();
        admin.setEmail("admin@smartclass.vn");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Quản trị viên");
        admin.setRole(User.Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);
        
        // Teacher
        User teacher = new User();
        teacher.setEmail("teacher@smartclass.vn");
        teacher.setPassword(passwordEncoder.encode("teacher123"));
        teacher.setFullName("Nguyễn Văn Giáo");
        teacher.setPhone("0901234567");
        teacher.setRole(User.Role.TEACHER);
        teacher.setActive(true);
        teacher.setBio("Giáo viên Toán, 10 năm kinh nghiệm");
        
        // Không gán gói nào - teacher sẽ bắt đầu với gói Miễn phí
        // teacher.setCurrentPlan(null);
        
        userRepository.save(teacher);
        
        // Student
        User student = new User();
        student.setEmail("student@smartclass.vn");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setFullName("Trần Thị Học");
        student.setRole(User.Role.STUDENT);
        student.setActive(true);
        userRepository.save(student);
        
        System.out.println("✅ Đã tạo 3 users mẫu:");
        System.out.println("   Admin: admin@smartclass.vn / admin123");
        System.out.println("   Teacher: teacher@smartclass.vn / teacher123");
        System.out.println("   Student: student@smartclass.vn / student123");
    }
    
    private void createSampleData() {
        User teacher = userRepository.findByEmail("teacher@smartclass.vn").orElse(null);
        if (teacher == null) return;
        
        // Tạo câu hỏi mẫu
        createSampleQuestions(teacher);
        
        // Tạo bài giảng mẫu
        createSampleLessons(teacher);
        
        // Tạo đề thi mẫu
        createSampleTests(teacher);
        
        // Tạo classrooms cho teacher
        createSampleClassrooms(teacher);
        
        // Gán bài giảng và đề thi cho student
        assignLessonsAndTestsToStudent(teacher);
        
        // KHÔNG tạo subscriptions mẫu - teacher sẽ tự đăng ký gói
        // createSampleSubscriptions();
        
        System.out.println("✅ Đã tạo data mẫu cho teacher");
    }
    
    private void createSampleClassrooms(User teacher) {
        // Classroom 1: Toán 1A
        Classroom classroom1 = new Classroom();
        classroom1.setTeacher(teacher);
        classroom1.setRoomId("TC001");
        classroom1.setName("Toán 1A");
        classroom1.setDescription("Lớp Toán dành cho học sinh lớp 1");
        classroom1.setSubject("Toán");
        classroom1.setGrade("Lớp 1");
        classroom1.setIsActive(true);
        classroomRepository.save(classroom1);
        
        // Classroom 2: Tiếng Việt 1B
        Classroom classroom2 = new Classroom();
        classroom2.setTeacher(teacher);
        classroom2.setRoomId("TC002");
        classroom2.setName("Tiếng Việt 1B");
        classroom2.setDescription("Lớp Tiếng Việt dành cho học sinh lớp 1");
        classroom2.setSubject("Tiếng Việt");
        classroom2.setGrade("Lớp 1");
        classroom2.setIsActive(true);
        classroomRepository.save(classroom2);
        
        System.out.println("✅ Đã tạo 2 classrooms mẫu");
    }
    
    private void createSampleSubscriptions() {
        User teacher = userRepository.findByEmail("teacher@smartclass.vn").orElse(null);
        if (teacher == null) return;
        
        Plan basicPlan = planRepository.findAll().stream()
            .filter(p -> p.getName().equals("Cơ bản"))
            .findFirst().orElse(null);
        
        if (basicPlan == null) return;
        
        // Tạo subscriptions trong 30 ngày qua
        for (int i = 0; i < 15; i++) {
            Subscription sub = new Subscription();
            sub.setUser(teacher);
            sub.setPlan(basicPlan);
            sub.setAmount(basicPlan.getPrice());
            sub.setStatus(Subscription.Status.ACTIVE);
            sub.setPaymentMethod("Credit Card");
            sub.setStartDate(LocalDateTime.now().minusDays(30 - i * 2));
            sub.setEndDate(LocalDateTime.now().plusMonths(1).minusDays(30 - i * 2));
            sub.setCreatedAt(LocalDateTime.now().minusDays(30 - i * 2));
            subscriptionRepository.save(sub);
        }
        
        System.out.println("✅ Đã tạo subscriptions mẫu");
    }
    
    private void assignLessonsAndTestsToStudent(User teacher) {
        User student = userRepository.findByEmail("student@smartclass.vn").orElse(null);
        if (student == null) return;
        
        // Gán tất cả bài giảng cho student
        List<Lesson> lessons = lessonRepository.findByTeacher(teacher);
        for (Lesson lesson : lessons) {
            StudentLesson sl = new StudentLesson();
            sl.setStudent(student);
            sl.setLesson(lesson);
            sl.setAssignedAt(LocalDateTime.now());
            studentLessonRepository.save(sl);
        }
        
        // Gán tất cả đề thi cho student
        List<Test> tests = testRepository.findByTeacher(teacher);
        for (Test test : tests) {
            StudentTest st = new StudentTest();
            st.setStudent(student);
            st.setTest(test);
            st.setAssignedAt(LocalDateTime.now());
            st.setStatus(StudentTest.Status.PENDING);
            studentTestRepository.save(st);
        }
        
        System.out.println("✅ Đã gán bài giảng và đề thi cho student");
    }
    
    private void createSampleQuestions(User teacher) {
        // ===== TOÁN LỚP 1 =====
        
        // Phép cộng - Dễ
        Question q1 = new Question();
        q1.setTeacher(teacher);
        q1.setContent("2 + 3 = ?");
        q1.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
        q1.setCorrectAnswer("5");
        q1.setExplanation("2 cộng 3 bằng 5");
        q1.setDifficulty(Question.Difficulty.EASY);
        q1.setSubject("Toán");
        q1.setGrade("Lớp 1");
        q1.setTopic("Phép cộng");
        q1.setReferenceBook("SGK Toán 1");
        q1.setReferencePage("15");
        q1.setPoints(1.0);
        q1.setCreatedBy(Question.CreatedBy.MANUAL);
        questionRepository.save(q1);
        
        Question q2 = new Question();
        q2.setTeacher(teacher);
        q2.setContent("4 + 5 = ?");
        q2.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
        q2.setCorrectAnswer("9");
        q2.setExplanation("4 cộng 5 bằng 9");
        q2.setDifficulty(Question.Difficulty.EASY);
        q2.setSubject("Toán");
        q2.setGrade("Lớp 1");
        q2.setTopic("Phép cộng");
        q2.setPoints(1.0);
        q2.setCreatedBy(Question.CreatedBy.MANUAL);
        questionRepository.save(q2);
        
        // Phép cộng - Trung bình
        Question q3 = new Question();
        q3.setTeacher(teacher);
        q3.setContent("Lan có 5 cái kẹo, mẹ cho thêm 3 cái. Hỏi Lan có tất cả bao nhiêu cái kẹo?");
        q3.setQuestionType(Question.QuestionType.ESSAY);
        q3.setCorrectAnswer("8 cái kẹo");
        q3.setExplanation("5 + 3 = 8. Lan có tất cả 8 cái kẹo.");
        q3.setDifficulty(Question.Difficulty.MEDIUM);
        q3.setSubject("Toán");
        q3.setGrade("Lớp 1");
        q3.setTopic("Phép cộng");
        q3.setReferenceBook("SGK Toán 1");
        q3.setReferencePage("28");
        q3.setPoints(2.0);
        q3.setCreatedBy(Question.CreatedBy.MANUAL);
        questionRepository.save(q3);
        
        // Phép trừ - Dễ
        Question q4 = new Question();
        q4.setTeacher(teacher);
        q4.setContent("8 - 3 = ?");
        q4.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
        q4.setCorrectAnswer("5");
        q4.setExplanation("8 trừ 3 bằng 5");
        q4.setDifficulty(Question.Difficulty.EASY);
        q4.setSubject("Toán");
        q4.setGrade("Lớp 1");
        q4.setTopic("Phép trừ");
        q4.setPoints(1.0);
        q4.setCreatedBy(Question.CreatedBy.MANUAL);
        questionRepository.save(q4);
        
        // Phép trừ - Trung bình
        Question q5 = new Question();
        q5.setTeacher(teacher);
        q5.setContent("Bình có 9 viên bi, cho bạn 4 viên. Hỏi Bình còn lại bao nhiêu viên bi?");
        q5.setQuestionType(Question.QuestionType.ESSAY);
        q5.setCorrectAnswer("5 viên bi");
        q5.setExplanation("9 - 4 = 5. Bình còn lại 5 viên bi.");
        q5.setDifficulty(Question.Difficulty.MEDIUM);
        q5.setSubject("Toán");
        q5.setGrade("Lớp 1");
        q5.setTopic("Phép trừ");
        q5.setPoints(2.0);
        q5.setCreatedBy(Question.CreatedBy.MANUAL);
        questionRepository.save(q5);
        
        // Phép trừ - Khó
        Question q6 = new Question();
        q6.setTeacher(teacher);
        q6.setContent("Trong một rổ có 10 quả táo. Bạn An lấy đi 3 quả, bạn Bình lấy đi 2 quả. Hỏi trong rổ còn lại bao nhiêu quả táo?");
        q6.setQuestionType(Question.QuestionType.ESSAY);
        q6.setCorrectAnswer("5 quả táo");
        q6.setExplanation("10 - 3 - 2 = 5. Trong rổ còn lại 5 quả táo.");
        q6.setDifficulty(Question.Difficulty.HARD);
        q6.setSubject("Toán");
        q6.setGrade("Lớp 1");
        q6.setTopic("Phép trừ");
        q6.setReferenceBook("SGK Toán 1");
        q6.setReferencePage("42");
        q6.setPoints(3.0);
        q6.setCreatedBy(Question.CreatedBy.MANUAL);
        questionRepository.save(q6);
        
        // ===== TIẾNG VIỆT LỚP 1 =====
        
        Question q7 = new Question();
        q7.setTeacher(teacher);
        q7.setContent("Chọn từ đúng: Con ... bay trên trời (chim/cá)");
        q7.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
        q7.setCorrectAnswer("chim");
        q7.setExplanation("Chim là loài động vật có cánh, bay được trên trời.");
        q7.setDifficulty(Question.Difficulty.EASY);
        q7.setSubject("Tiếng Việt");
        q7.setGrade("Lớp 1");
        q7.setTopic("Nhận biết chữ cái");
        q7.setReferenceBook("SGK Tiếng Việt 1");
        q7.setReferencePage("20");
        q7.setPoints(1.0);
        q7.setCreatedBy(Question.CreatedBy.MANUAL);
        questionRepository.save(q7);
        
        // ===== TIẾNG ANH LỚP 3 =====
        
        Question q8 = new Question();
        q8.setTeacher(teacher);
        q8.setContent("What color is the sky? (Bầu trời màu gì?)");
        q8.setQuestionType(Question.QuestionType.ESSAY);
        q8.setCorrectAnswer("Blue (Xanh)");
        q8.setExplanation("The sky is blue. Bầu trời màu xanh.");
        q8.setDifficulty(Question.Difficulty.EASY);
        q8.setSubject("Tiếng Anh");
        q8.setGrade("Lớp 3");
        q8.setTopic("Colors - Màu sắc");
        q8.setReferenceBook("SGK Tiếng Anh 3");
        q8.setReferencePage("12");
        q8.setPoints(1.0);
        q8.setCreatedBy(Question.CreatedBy.MANUAL);
        questionRepository.save(q8);
    }
    
    private void createSampleLessons(User teacher) {
        // Bài giảng Toán
        Lesson lesson1 = new Lesson();
        lesson1.setTeacher(teacher);
        lesson1.setTitle("Phép cộng trong phạm vi 10");
        lesson1.setSubject("Toán");
        lesson1.setGrade("Lớp 1");
        lesson1.setTopic("Phép cộng");
        lesson1.setLearningObjectives("Học sinh biết cách cộng các số trong phạm vi 10");
        lesson1.setContent("# Phép cộng trong phạm vi 10\n\n" +
            "## Mục tiêu\n" +
            "- Nhận biết phép cộng\n" +
            "- Thực hiện phép cộng các số từ 0 đến 10\n\n" +
            "## Nội dung\n" +
            "1. Giới thiệu phép cộng\n" +
            "2. Thực hành với đồ vật cụ thể\n" +
            "3. Làm bài tập\n\n" +
            "## Ví dụ\n" +
            "- 1 + 1 = 2\n" +
            "- 2 + 3 = 5\n" +
            "- 4 + 5 = 9");
        lessonRepository.save(lesson1);
        
        // Bài giảng Tiếng Việt
        Lesson lesson2 = new Lesson();
        lesson2.setTeacher(teacher);
        lesson2.setTitle("Học chữ cái tiếng Việt");
        lesson2.setSubject("Tiếng Việt");
        lesson2.setGrade("Lớp 1");
        lesson2.setTopic("Bảng chữ cái");
        lesson2.setLearningObjectives("Học sinh nhận biết và đọc được các chữ cái a, b, c");
        lesson2.setContent("# Học chữ cái tiếng Việt\n\n" +
            "## Mục tiêu\n" +
            "- Nhận biết chữ cái a, b, c\n" +
            "- Đọc đúng các chữ cái\n\n" +
            "## Nội dung\n" +
            "1. Giới thiệu chữ cái a\n" +
            "2. Giới thiệu chữ cái b\n" +
            "3. Giới thiệu chữ cái c\n" +
            "4. Luyện đọc");
        lessonRepository.save(lesson2);
    }
    
    private void createSampleTests(User teacher) {
        // Lấy các câu hỏi đã tạo
        List<Question> allQuestions = questionRepository.findByTeacher(teacher);
        
        // Đề thi Toán - có câu hỏi đầy đủ
        Test test1 = new Test();
        test1.setTeacher(teacher);
        test1.setTitle("Kiểm tra Toán - Phép cộng và phép trừ");
        test1.setSubject("Toán");
        test1.setGrade("Lớp 1");
        test1.setDurationMinutes(45);
        test1.setTestMatrix("{\n" +
            "  \"matrix\": [\n" +
            "    {\"topic\": \"Phép cộng\", \"easy\": 2, \"medium\": 1, \"hard\": 0},\n" +
            "    {\"topic\": \"Phép trừ\", \"easy\": 1, \"medium\": 1, \"hard\": 1}\n" +
            "  ]\n" +
            "}");
        
        // Tính tổng điểm và thêm câu hỏi
        double totalPoints = 0;
        Test savedTest1 = testRepository.save(test1);
        
        int orderNum = 1;
        for (Question q : allQuestions) {
            if (q.getSubject().equals("Toán")) {
                TestQuestion tq = new TestQuestion();
                tq.setTest(savedTest1);
                tq.setQuestion(q);
                tq.setOrderNumber(orderNum++);
                tq.setCustomPoints(q.getPoints());
                savedTest1.getTestQuestions().add(tq);
                totalPoints += q.getPoints();
            }
        }
        
        savedTest1.setTotalPoints(totalPoints);
        testRepository.save(savedTest1);
        
        // Đề thi Tiếng Việt
        Test test2 = new Test();
        test2.setTeacher(teacher);
        test2.setTitle("Kiểm tra Tiếng Việt - Bảng chữ cái");
        test2.setSubject("Tiếng Việt");
        test2.setGrade("Lớp 1");
        test2.setDurationMinutes(30);
        test2.setTestMatrix("{\n" +
            "  \"matrix\": [\n" +
            "    {\"topic\": \"Nhận biết chữ cái\", \"easy\": 3, \"medium\": 2, \"hard\": 0}\n" +
            "  ]\n" +
            "}");
        
        totalPoints = 0;
        Test savedTest2 = testRepository.save(test2);
        
        orderNum = 1;
        for (Question q : allQuestions) {
            if (q.getSubject().equals("Tiếng Việt")) {
                TestQuestion tq = new TestQuestion();
                tq.setTest(savedTest2);
                tq.setQuestion(q);
                tq.setOrderNumber(orderNum++);
                tq.setCustomPoints(q.getPoints());
                savedTest2.getTestQuestions().add(tq);
                totalPoints += q.getPoints();
            }
        }
        
        savedTest2.setTotalPoints(totalPoints);
        testRepository.save(savedTest2);
    }
}
