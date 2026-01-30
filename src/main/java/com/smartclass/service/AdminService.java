package com.smartclass.service;

import com.smartclass.model.*;
import com.smartclass.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class
AdminService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final TestRepository testRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ClassroomRepository classroomRepository;
    private final AIHistoryRepository aiHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== DASHBOARD STATISTICS ==========

    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // User stats
        List<User> allUsers = userRepository.findAll();
        stats.put("totalUsers", allUsers.size());
        stats.put("teachers", allUsers.stream().filter(u -> u.getRole() == User.Role.TEACHER).count());
        stats.put("students", allUsers.stream().filter(u -> u.getRole() == User.Role.STUDENT).count());

        // Content stats
        stats.put("totalQuestions", questionRepository.count());
        stats.put("totalLessons", lessonRepository.count());
        stats.put("totalTests", testRepository.count());
        stats.put("totalClassrooms", classroomRepository.count());

        // Subscription stats
//        stats.put("activeSubscriptions", subscriptionRepository.countByStatus(Subscription.Status.ACTIVE));
//
//        // Revenue stats
//        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
//        Double monthlyRevenue = subscriptionRepository.getTotalRevenue(thirtyDaysAgo);
//        stats.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);
//
//        // AI Usage stats
//        stats.put("totalAIUsages", aiHistoryRepository.count());
//
//        // Recent subscriptions
//        List<Subscription> recentSubscriptions = subscriptionRepository.findRecentSubscriptions(
//            LocalDateTime.now().minusDays(7)
//        );
//        stats.put("recentSubscriptions", recentSubscriptions);

        return stats;
    }

    // ========== USER MANAGEMENT ==========

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ngườii dùng"));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setRole(userDetails.getRole());
        user.setActive(userDetails.getActive());
        
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User toggleUserStatus(Long id) {
        User user = getUserById(id);
        user.setActive(!user.getActive());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void resetUserPassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ========== PLAN MANAGEMENT ==========

//    public List<Plan> getAllPlans() {
//        return planRepository.findAll();
//    }
//
//    public Plan getPlanById(Long id) {
//        return planRepository.findById(id)
//            .orElseThrow(() -> new RuntimeException("Không tìm thấy gói dịch vụ"));
//    }
//
//    @Transactional
//    public Plan createPlan(Plan plan) {
//        plan.setActive(true);
//        return planRepository.save(plan);
//    }
//
//    @Transactional
//    public Plan updatePlan(Long id, Plan planDetails) {
//        Plan plan = getPlanById(id);
//        plan.setName(planDetails.getName());
//        plan.setDescription(planDetails.getDescription());
//        plan.setPrice(planDetails.getPrice());
//        plan.setMaxClasses(planDetails.getMaxClasses());
//        plan.setMaxStudents(planDetails.getMaxStudents());
//        plan.setAiLessonsPerMonth(planDetails.getAiLessonsPerMonth());
//        plan.setTestsPerMonth(planDetails.getTestsPerMonth());
//        plan.setMaxQuestionsBank(planDetails.getMaxQuestionsBank());
//        plan.setAutoGrading(planDetails.getAutoGrading());
//        plan.setAdvancedReports(planDetails.getAdvancedReports());
//        plan.setActive(planDetails.getActive());
//        return planRepository.save(plan);
//    }
//
//    @Transactional
//    public void deletePlan(Long id) {
//        Plan plan = getPlanById(id);
//
//        // Check if plan is in use
//        List<Subscription> subscriptions = subscriptionRepository.findByPlan(plan);
//        if (!subscriptions.isEmpty()) {
//            throw new RuntimeException("Không thể xóa gói này vì đang có ngườii dùng sử dụng!");
//        }
//
//        planRepository.deleteById(id);
//    }
//
//    @Transactional
//    public Plan togglePlanStatus(Long id) {
//        Plan plan = getPlanById(id);
//        plan.setActive(!plan.getActive());
//        return planRepository.save(plan);
//    }

    // ========== PAYMENT MANAGEMENT ==========

//    public Map<String, Object> getPaymentStatistics() {
//        Map<String, Object> stats = new HashMap<>();
//        List<Subscription> subscriptions = subscriptionRepository.findAll();
//        subscriptions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
//
//        long paidCount = subscriptions.stream()
//            .filter(s -> s.getPaymentStatus() == Subscription.PaymentStatus.PAID)
//            .count();
//        long pendingCount = subscriptions.stream()
//            .filter(s -> s.getPaymentStatus() == Subscription.PaymentStatus.PENDING)
//            .count();
//        long failedCount = subscriptions.stream()
//            .filter(s -> s.getPaymentStatus() == Subscription.PaymentStatus.FAILED)
//            .count();
//        double totalRevenue = subscriptions.stream()
//            .filter(s -> s.getPaymentStatus() == Subscription.PaymentStatus.PAID)
//            .mapToDouble(s -> s.getAmount() != null ? s.getAmount() : 0.0)
//            .sum();
//
//        stats.put("subscriptions", subscriptions);
//        stats.put("paidCount", paidCount);
//        stats.put("pendingCount", pendingCount);
//        stats.put("failedCount", failedCount);
//        stats.put("totalRevenue", totalRevenue);
//
//        return stats;
//    }
//
//    @Transactional
//    public void confirmPayment(Long subscriptionId) {
//        Subscription subscription = subscriptionRepository.findById(subscriptionId)
//            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
//
//        // Cancel any existing active subscriptions for this user
//        List<Subscription> activeSubscriptions = subscriptionRepository
//            .findByUserAndStatus(subscription.getUser(), Subscription.Status.ACTIVE);
//
//        for (Subscription activeSub : activeSubscriptions) {
//            if (!activeSub.getId().equals(subscriptionId)) {
//                activeSub.setStatus(Subscription.Status.CANCELLED);
//                subscriptionRepository.save(activeSub);
//            }
//        }
//
//        // Activate new subscription
//        subscription.setPaymentStatus(Subscription.PaymentStatus.PAID);
//        subscription.setStatus(Subscription.Status.ACTIVE);
//        subscriptionRepository.save(subscription);
//
//        // Update user's current plan
//        User user = subscription.getUser();
//        user.setCurrentPlan(subscription.getPlan());
//        userRepository.save(user);
//    }
//
//    @Transactional
//    public void cancelPayment(Long subscriptionId) {
//        Subscription subscription = subscriptionRepository.findById(subscriptionId)
//            .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
//
//        subscription.setPaymentStatus(Subscription.PaymentStatus.FAILED);
//        subscription.setStatus(Subscription.Status.CANCELLED);
//        subscriptionRepository.save(subscription);
//    }

    // ========== QUESTION MANAGEMENT ==========

    public List<Question> getAllQuestions(String subject, String grade, String keyword) {
        List<Question> questions;
        
        if (subject != null || grade != null || keyword != null) {
            questions = questionRepository.searchQuestionsAdmin(
                subject != null && !subject.isEmpty() ? subject : null,
                grade != null && !grade.isEmpty() ? grade : null,
                keyword != null && !keyword.isEmpty() ? keyword : null
            );
        } else {
            questions = questionRepository.findAll();
        }
        
        questions.sort((q1, q2) -> q2.getId().compareTo(q1.getId()));
        return questions;
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    // ========== LESSON MANAGEMENT ==========

    public List<Lesson> getAllLessons(String subject, String grade) {
        List<Lesson> lessons;
        
        if ((subject != null && !subject.isEmpty()) || (grade != null && !grade.isEmpty())) {
            lessons = lessonRepository.searchLessonsAdmin(
                subject != null && !subject.isEmpty() ? subject : null,
                grade != null && !grade.isEmpty() ? grade : null
            );
        } else {
            lessons = lessonRepository.findAll();
        }
        
        lessons.sort((l1, l2) -> l2.getId().compareTo(l1.getId()));
        return lessons;
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài giảng"));
    }

    @Transactional
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    // ========== TEST MANAGEMENT ==========

    public List<Test> getAllTests(String subject, String grade) {
        List<Test> tests;
        
        if ((subject != null && !subject.isEmpty()) || (grade != null && !grade.isEmpty())) {
            tests = testRepository.searchTestsAdmin(
                subject != null && !subject.isEmpty() ? subject : null,
                grade != null && !grade.isEmpty() ? grade : null
            );
        } else {
            tests = testRepository.findAll();
        }
        
        tests.sort((t1, t2) -> t2.getId().compareTo(t1.getId()));
        return tests;
    }

    public Test getTestById(Long id) {
        return testRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));
    }

    @Transactional
    public void deleteTest(Long id) {
        testRepository.deleteById(id);
    }

    // ========== CLASSROOM MANAGEMENT ==========

    public List<Classroom> getAllClassrooms() {
        List<Classroom> classrooms = classroomRepository.findAll();
        classrooms.sort((c1, c2) -> c2.getId().compareTo(c1.getId()));
        return classrooms;
    }

    public Classroom getClassroomById(Long id) {
        return classroomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học"));
    }

    public Map<Long, Long> getClassroomMemberCounts(List<Classroom> classrooms) {
        Map<Long, Long> memberCounts = new HashMap<>();
        for (Classroom classroom : classrooms) {
            long count = classroom.getMembers() != null ?
                classroom.getMembers().stream()
                    .filter(m -> m.getStatus() == ClassroomMember.Status.ACTIVE)
                    .count() : 0;
            memberCounts.put(classroom.getId(), count);
        }
        return memberCounts;
    }

    public List<ClassroomMember> getActiveClassroomMembers(Classroom classroom) {
        return classroom.getMembers() != null ?
            classroom.getMembers().stream()
                .filter(m -> m.getStatus() == ClassroomMember.Status.ACTIVE)
                .toList() : new ArrayList<>();
    }

    @Transactional
    public void deleteClassroom(Long id) {
        classroomRepository.deleteById(id);
    }

    // ========== AI HISTORY MANAGEMENT ==========

    public List<AIHistory> getAllAIHistory(String type, String keyword) {
        List<AIHistory> histories;
        
        if (type != null && !type.isEmpty()) {
            try {
                AIHistory.AIType aiType = AIHistory.AIType.valueOf(type);
                histories = aiHistoryRepository.findByAiTypeOrderByCreatedAtDesc(aiType);
            } catch (IllegalArgumentException e) {
                histories = aiHistoryRepository.findAllOrderByCreatedAtDesc();
            }
        } else {
            histories = aiHistoryRepository.findAllOrderByCreatedAtDesc();
        }

        // Filter by keyword if provided
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            histories = histories.stream()
                .filter(h -> (h.getSubject() != null && h.getSubject().toLowerCase().contains(lowerKeyword)) ||
                            (h.getTopic() != null && h.getTopic().toLowerCase().contains(lowerKeyword)) ||
                            (h.getTeacher() != null && h.getTeacher().getFullName().toLowerCase().contains(lowerKeyword)))
                .toList();
        }

        return histories;
    }

    @Transactional
    public void deleteAIHistory(Long id) {
        aiHistoryRepository.deleteById(id);
    }

    // ========== PROFILE MANAGEMENT ==========

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ngườii dùng"));
    }

    @Transactional
    public User updateProfile(String email, User userDetails) {
        User user = getUserByEmail(email);
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setBio(userDetails.getBio());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
