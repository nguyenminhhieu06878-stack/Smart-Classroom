package com.smartclass.controller;

import com.smartclass.model.*;
import com.smartclass.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/teacher")
public class TeacherController {
    private static final Logger log = LoggerFactory.getLogger(TeacherController.class);

    private final UserService userService;
    private final QuestionService questionService;
    private final LessonService lessonService;
    private final TestService testService;
    private final AIService aiService;
    private final PaymentService paymentService;
    private final ClassroomService classroomService;
    private final ClassroomMemberService classroomMemberService;
    private final SubscriptionService subscriptionService;

    public TeacherController(UserService userService, QuestionService questionService,
            LessonService lessonService, TestService testService,
            AIService aiService, PaymentService paymentService,
            ClassroomService classroomService, ClassroomMemberService classroomMemberService,
            SubscriptionService subscriptionService) {
        this.userService = userService;
        this.questionService = questionService;
        this.lessonService = lessonService;
        this.testService = testService;
        this.aiService = aiService;
        this.paymentService = paymentService;
        this.classroomService = classroomService;
        this.classroomMemberService = classroomMemberService;
        this.subscriptionService = subscriptionService;
    }

    private User getCurrentUser(Authentication auth) {
        return userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);

        long questionCount = questionService.countQuestionsByTeacher(teacher);
        long lessonCount = lessonService.countLessonsByTeacher(teacher);
        long testCount = testService.countTestsByTeacher(teacher);
        long classroomCount = classroomService.countByTeacher(teacher);

        // Tính tổng học sinh từ tất cả các phòng
        List<Classroom> classrooms = classroomService.findByTeacherAndIsActive(teacher, true);
        long totalStudents = classrooms.stream()
                .mapToLong(c -> classroomMemberService.countByClassroomAndStatus(c, ClassroomMember.Status.ACTIVE))
                .sum();

        // Get active subscription for usage warnings
        Subscription activeSubscription = paymentService.getActiveSubscription(teacher);
        if (activeSubscription != null) {
            // Check for warnings
            long daysRemaining = activeSubscription.getDaysRemaining();
            if (daysRemaining <= 7 && daysRemaining > 0) {
                model.addAttribute("warningMessage",
                        "Gói dịch vụ của bạn sẽ hết hạn trong " + daysRemaining
                                + " ngày. Vui lòng gia hạn để tiếp tục sử dụng.");
            }

            // Check AI lesson usage
            Integer aiLessonsUsed = activeSubscription.getAiLessonsUsed() != null
                    ? activeSubscription.getAiLessonsUsed()
                    : 0;
            Integer aiLessonsLimit = activeSubscription.getPlan().getAiLessonsPerMonth();
            if (aiLessonsLimit != null && aiLessonsLimit > 0) {
                double usagePercent = (aiLessonsUsed * 100.0) / aiLessonsLimit;
                if (usagePercent >= 80) {
                    model.addAttribute("aiLessonWarning",
                            "Bạn đã sử dụng " + aiLessonsUsed + "/" + aiLessonsLimit + " lượt tạo bài giảng AI.");
                }
            }

            // Check test creation usage
            Integer testsCreated = activeSubscription.getTestsCreated() != null ? activeSubscription.getTestsCreated()
                    : 0;
            Integer testsLimit = activeSubscription.getPlan().getTestsPerMonth();
            if (testsLimit != null && testsLimit > 0) {
                double usagePercent = (testsCreated * 100.0) / testsLimit;
                if (usagePercent >= 80) {
                    model.addAttribute("testWarning",
                            "Bạn đã tạo " + testsCreated + "/" + testsLimit + " đề thi trong tháng.");
                }
            }
        }

        model.addAttribute("teacher", teacher);
        model.addAttribute("questionCount", questionCount);
        model.addAttribute("lessonCount", lessonCount);
        model.addAttribute("testCount", testCount);
        model.addAttribute("classroomCount", classroomCount);
        model.addAttribute("studentCount", totalStudents);
        model.addAttribute("activeSubscription", activeSubscription);

        return "teacher/dashboard";
    }

    // ========== NGÂN HÀNG CÂU HỎI ==========

    @GetMapping("/questions")
    public String questions(Authentication auth, Model model,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            User teacher = getCurrentUser(auth);
            System.out.println("=== DEBUG: Getting questions for teacher: " + teacher.getEmail());

            // Convert empty strings to null
            subject = (subject != null && subject.trim().isEmpty()) ? null : subject;
            grade = (grade != null && grade.trim().isEmpty()) ? null : grade;
            difficulty = (difficulty != null && difficulty.trim().isEmpty()) ? null : difficulty;
            keyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;

            System.out.println("=== DEBUG: Filters - subject: " + subject + ", grade: " + grade + ", difficulty: "
                    + difficulty + ", keyword: " + keyword);

            // Get all questions first
            List<Question> allQuestions;
            if (subject != null || grade != null || difficulty != null || keyword != null) {
                Question.Difficulty diff = difficulty != null ? Question.Difficulty.valueOf(difficulty) : null;
                allQuestions = questionService.searchQuestions(teacher, subject, grade, diff, keyword);
            } else {
                allQuestions = questionService.getQuestionsByTeacher(teacher);
            }

            System.out.println("=== DEBUG: Found " + allQuestions.size() + " questions");

            // Sort by ID descending
            if (!allQuestions.isEmpty()) {
                allQuestions.sort((q1, q2) -> q2.getId().compareTo(q1.getId()));
            }

            // Manual pagination
            int totalItems = allQuestions.size();
            int totalPages = totalItems > 0 ? (int) Math.ceil((double) totalItems / size) : 0;
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalItems);

            List<Question> pageQuestions = (fromIndex < totalItems) ? allQuestions.subList(fromIndex, toIndex)
                    : new ArrayList<>();

            System.out.println("=== DEBUG: Page " + page + " has " + pageQuestions.size() + " questions");

            model.addAttribute("questions", pageQuestions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("pageSize", size);
            model.addAttribute("subject", subject);
            model.addAttribute("grade", grade);
            model.addAttribute("difficulty", difficulty);
            model.addAttribute("keyword", keyword);

            System.out.println("=== DEBUG: Rendering template");
            return "teacher/questions";
        } catch (Exception e) {
            System.err.println("=== ERROR in questions controller:");
            e.printStackTrace();
            throw new RuntimeException("Error loading questions", e);
        }
    }

    @GetMapping("/questions/new")
    public String newQuestion(Model model) {
        model.addAttribute("question", new Question());
        return "teacher/question-form";
    }

    @PostMapping("/questions")
    public String createQuestion(@ModelAttribute Question question, Authentication auth) {
        User teacher = getCurrentUser(auth);
        question.setTeacher(teacher);
        questionService.createQuestion(question);
        return "redirect:/teacher/questions";
    }

    @GetMapping("/questions/{id}/edit")
    public String editQuestion(@PathVariable Long id, Model model) {
        Question question = questionService.getQuestionById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        model.addAttribute("question", question);
        return "teacher/question-form";
    }

    @PostMapping("/questions/{id}")
    public String updateQuestion(@PathVariable Long id, @ModelAttribute Question question) {
        questionService.updateQuestion(id, question);
        return "redirect:/teacher/questions";
    }

    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return "redirect:/teacher/questions";
    }

    // ========== AI TẠO CÂU HỎI ==========

    @GetMapping("/questions/ai-generate")
    public String aiGenerateForm(Model model) {
        return "teacher/ai-generate-questions";
    }

    @PostMapping("/questions/ai-generate")
    public String aiGenerateQuestions(@RequestParam String subject,
            @RequestParam String grade,
            @RequestParam String topic,
            @RequestParam int easyCount,
            @RequestParam int mediumCount,
            @RequestParam int hardCount,
            Authentication auth) {
        User teacher = getCurrentUser(auth);

        List<Map<String, Object>> generatedQuestions = aiService.generateQuestions(
                subject, grade, topic, easyCount, mediumCount, hardCount, teacher);

        // Lưu vào database
        for (Map<String, Object> qData : generatedQuestions) {
            Question question = new Question();
            question.setTeacher(teacher);
            question.setContent((String) qData.get("content"));
            question.setCorrectAnswer((String) qData.get("correctAnswer"));
            question.setExplanation((String) qData.get("explanation"));
            question.setDifficulty(Question.Difficulty.valueOf((String) qData.get("difficulty")));
            question.setSubject(subject);
            question.setGrade(grade);
            question.setTopic(topic);
            question.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
            question.setCreatedBy(Question.CreatedBy.AI);
            question.setPoints(((Number) qData.get("points")).doubleValue());

            // Lưu options cho trắc nghiệm
            if (qData.containsKey("optionA"))
                question.setOptionA((String) qData.get("optionA"));
            if (qData.containsKey("optionB"))
                question.setOptionB((String) qData.get("optionB"));
            if (qData.containsKey("optionC"))
                question.setOptionC((String) qData.get("optionC"));
            if (qData.containsKey("optionD"))
                question.setOptionD((String) qData.get("optionD"));

            questionService.createQuestion(question);
        }

        return "redirect:/teacher/questions";
    }

    // ========== BÀI GIẢNG ==========

    @GetMapping("/lessons")
    public String lessons(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<Lesson> lessons = lessonService.getLessonsByTeacher(teacher);
        model.addAttribute("lessons", lessons);
        return "teacher/lessons";
    }

    @GetMapping("/lessons/new")
    public String newLesson(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<Classroom> classrooms = classroomService.findByTeacherAndIsActive(teacher, true);
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("classrooms", classrooms);
        return "teacher/lesson-form";
    }

    @PostMapping("/lessons")
    public String createLesson(@ModelAttribute Lesson lesson, Authentication auth) {
        User teacher = getCurrentUser(auth);
        lesson.setTeacher(teacher);
        lessonService.createLesson(lesson);
        return "redirect:/teacher/lessons";
    }

    @GetMapping("/lessons/{id}/edit")
    public String editLesson(@PathVariable Long id, Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<Classroom> classrooms = classroomService.findByTeacherAndIsActive(teacher, true);
        Lesson lesson = lessonService.getLessonById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        model.addAttribute("lesson", lesson);
        model.addAttribute("classrooms", classrooms);
        return "teacher/lesson-form";
    }

    @PostMapping("/lessons/{id}")
    public String updateLesson(@PathVariable Long id, @ModelAttribute Lesson lesson, Authentication auth) {
        User teacher = getCurrentUser(auth);
        lesson.setTeacher(teacher);
        lessonService.updateLesson(id, lesson);
        return "redirect:/teacher/lessons";
    }

    @PostMapping("/lessons/{id}/delete")
    public String deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return "redirect:/teacher/lessons";
    }

    @GetMapping("/lessons/{id}")
    public String viewLesson(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getLessonById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        model.addAttribute("lesson", lesson);
        return "teacher/lesson-detail";
    }

    @GetMapping("/lessons/ai-generate")
    public String aiGenerateLessonForm(Model model) {
        return "teacher/ai-generate-lesson";
    }

    @PostMapping("/lessons/ai-generate")
    public String aiGenerateLesson(@RequestParam String subject,
            @RequestParam String grade,
            @RequestParam String topic,
            @RequestParam String objectives,
            Authentication auth,
            Model model) {
        User teacher = getCurrentUser(auth);

        // Check if user can use AI lesson feature
        if (!paymentService.canUseAILesson(teacher)) {
            model.addAttribute("errorMessage",
                    "Bạn đã hết lượt tạo bài giảng AI trong tháng này. Vui lòng nâng cấp gói dịch vụ.");
            return "teacher/ai-generate-lesson";
        }

        String content = aiService.generateLesson(subject, grade, topic, objectives, teacher);

        // Track AI usage
        paymentService.trackAILessonUsage(teacher);

        Lesson lesson = new Lesson();
        lesson.setTitle(topic);
        lesson.setSubject(subject);
        lesson.setGrade(grade);
        lesson.setTopic(topic);
        lesson.setLearningObjectives(objectives);
        lesson.setContent(content);

        // Add classrooms list so user can select a classroom
        List<Classroom> classrooms = classroomService.findByTeacherAndIsActive(teacher, true);

        model.addAttribute("lesson", lesson);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("generated", true);
        return "teacher/lesson-form";
    }

    // ========== ĐỀ THI ==========

    @GetMapping("/tests")
    public String tests(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<Test> tests = testService.getTestsByTeacher(teacher);
        model.addAttribute("tests", tests);
        return "teacher/tests";
    }

    @GetMapping("/tests/new")
    public String newTest(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<Classroom> classrooms = classroomService.findByTeacherAndIsActive(teacher, true);
        model.addAttribute("test", new Test());
        model.addAttribute("classrooms", classrooms);
        return "teacher/test-form";
    }

    @GetMapping("/tests/{id}")
    public String viewTest(@PathVariable Long id, Model model) {
        Test test = testService.getTestById(id)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        model.addAttribute("test", test);
        return "teacher/test-detail";
    }

    @GetMapping("/tests/{id}/edit")
    public String editTest(@PathVariable Long id, Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<Classroom> classrooms = classroomService.findByTeacherAndIsActive(teacher, true);
        Test test = testService.getTestById(id)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        model.addAttribute("test", test);
        model.addAttribute("classrooms", classrooms);
        return "teacher/test-form";
    }

    @PostMapping("/tests/{id}")
    public String updateTest(@PathVariable Long id, @ModelAttribute Test test, Authentication auth) {
        User teacher = getCurrentUser(auth);
        test.setTeacher(teacher);
        testService.updateTest(id, test);
        return "redirect:/teacher/tests";
    }

    @PostMapping("/tests/{id}/delete")
    public String deleteTest(@PathVariable Long id) {
        testService.deleteTest(id);
        return "redirect:/teacher/tests";
    }

    @PostMapping("/tests")
    public String createTest(@ModelAttribute Test test, Authentication auth) {
        User teacher = getCurrentUser(auth);
        test.setTeacher(teacher);
        testService.createTest(test);
        return "redirect:/teacher/tests";
    }

    @GetMapping("/tests/ai-generate")
    public String aiGenerateTestForm(Model model) {
        return "teacher/ai-generate-test";
    }

    @PostMapping("/tests/ai-generate")
    public String aiGenerateTest(@RequestParam(required = false) String title,
            @RequestParam String subject,
            @RequestParam String grade,
            @RequestParam String topic,
            @RequestParam Integer duration,
            @RequestParam int easyCount,
            @RequestParam int mediumCount,
            @RequestParam int hardCount,
            Authentication auth,
            Model model) {
        User teacher = getCurrentUser(auth);

        // Check if user can use AI test feature
        if (!paymentService.canUseAITest(teacher)) {
            model.addAttribute("errorMessage",
                    "Bạn đã hết lượt tạo đề thi AI trong tháng này. Vui lòng nâng cấp gói dịch vụ.");
            return "teacher/ai-generate-test";
        }

        // Tạo câu hỏi với AI
        List<Map<String, Object>> generatedQuestions = aiService.generateQuestions(
                subject, grade, topic, easyCount, mediumCount, hardCount, teacher);

        // Track AI usage
        paymentService.trackAITestUsage(teacher);
        paymentService.trackAIQuestionsUsage(teacher, generatedQuestions.size());

        // Tạo đề thi
        Test test = new Test();
        test.setTeacher(teacher);
        test.setTitle(title != null && !title.isEmpty() ? title : "Đề thi " + subject + " - " + topic);
        test.setSubject(subject);
        test.setGrade(grade);
        test.setDurationMinutes(duration);

        // Tạo ma trận đề thi
        StringBuilder matrixJson = new StringBuilder("{\"matrix\":[");
        matrixJson.append(String.format(
                "{\"topic\":\"%s\",\"easy\":%d,\"medium\":%d,\"hard\":%d}",
                topic, easyCount, mediumCount, hardCount));
        matrixJson.append("]}");
        test.setTestMatrix(matrixJson.toString());

        // Tính tổng điểm
        double totalPoints = 0;
        for (Map<String, Object> qData : generatedQuestions) {
            totalPoints += ((Number) qData.get("points")).doubleValue();
        }
        test.setTotalPoints(totalPoints);

        // Lưu đề thi
        Test savedTest = testService.createTest(test);

        // Track test creation
        paymentService.trackTestCreation(teacher);

        // Lưu câu hỏi vào database và thêm vào đề thi
        int orderNumber = 1;
        for (Map<String, Object> qData : generatedQuestions) {
            // Tạo câu hỏi mới
            Question question = new Question();
            question.setTeacher(teacher);
            question.setContent((String) qData.get("content"));
            question.setCorrectAnswer((String) qData.get("correctAnswer"));
            question.setExplanation((String) qData.get("explanation"));
            question.setDifficulty(Question.Difficulty.valueOf((String) qData.get("difficulty")));
            question.setSubject(subject);
            question.setGrade(grade);
            question.setTopic(topic);
            question.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
            question.setCreatedBy(Question.CreatedBy.AI);
            question.setPoints(((Number) qData.get("points")).doubleValue());

            // Lưu options
            if (qData.containsKey("optionA"))
                question.setOptionA((String) qData.get("optionA"));
            if (qData.containsKey("optionB"))
                question.setOptionB((String) qData.get("optionB"));
            if (qData.containsKey("optionC"))
                question.setOptionC((String) qData.get("optionC"));
            if (qData.containsKey("optionD"))
                question.setOptionD((String) qData.get("optionD"));

            Question savedQuestion = questionService.createQuestion(question);

            // Thêm vào đề thi
            TestQuestion tq = new TestQuestion();
            tq.setTest(savedTest);
            tq.setQuestion(savedQuestion);
            tq.setOrderNumber(orderNumber++);
            tq.setCustomPoints(savedQuestion.getPoints());
            savedTest.getTestQuestions().add(tq);
        }

        testService.updateTest(savedTest.getId(), savedTest);

        return "redirect:/teacher/tests/" + savedTest.getId();
    }

    // ========== TẠO ĐỀ THI TỪ NGÂN HÀNG (3 BƯỚC) ==========

    @GetMapping("/tests/builder/step1")
    public String testBuilderStep1() {
        return "teacher/test-builder-step1";
    }

    @PostMapping("/tests/builder/step2")
    public String testBuilderStep2(@RequestParam String title,
            @RequestParam String subject,
            @RequestParam String grade,
            @RequestParam Integer duration,
            @RequestParam String questionSource,
            Model model) {
        // If AI only, redirect to AI generate page with pre-filled data
        if ("ai".equals(questionSource)) {
            model.addAttribute("title", title);
            model.addAttribute("subject", subject);
            model.addAttribute("grade", grade);
            model.addAttribute("duration", duration);
            return "teacher/ai-generate-test";
        }

        // Otherwise continue with matrix builder
        model.addAttribute("title", title);
        model.addAttribute("subject", subject);
        model.addAttribute("grade", grade);
        model.addAttribute("duration", duration);
        model.addAttribute("questionSource", questionSource);
        return "teacher/test-builder-step2";
    }

    @PostMapping("/tests/builder/step3")
    public String testBuilderStep3(@RequestParam String title,
            @RequestParam String subject,
            @RequestParam String grade,
            @RequestParam Integer duration,
            @RequestParam String questionSource,
            @RequestParam("topics[]") List<String> topics,
            @RequestParam("easy[]") List<Integer> easyCounts,
            @RequestParam("medium[]") List<Integer> mediumCounts,
            @RequestParam("hard[]") List<Integer> hardCounts,
            Authentication auth,
            Model model) {
        User teacher = getCurrentUser(auth);

        // Build matrix JSON
        StringBuilder matrixJson = new StringBuilder("{\"matrix\":[");
        for (int i = 0; i < topics.size(); i++) {
            if (i > 0)
                matrixJson.append(",");
            matrixJson.append(String.format(
                    "{\"topic\":\"%s\",\"easy\":%d,\"medium\":%d,\"hard\":%d}",
                    topics.get(i), easyCounts.get(i), mediumCounts.get(i), hardCounts.get(i)));
        }
        matrixJson.append("]}");

        // Get questions for each section based on questionSource
        List<Map<String, Object>> matrixSections = new ArrayList<>();
        for (int i = 0; i < topics.size(); i++) {
            Map<String, Object> section = new HashMap<>();
            section.put("topic", topics.get(i));
            section.put("easyCount", easyCounts.get(i));
            section.put("mediumCount", mediumCounts.get(i));
            section.put("hardCount", hardCounts.get(i));

            List<Question> easyQuestions = new ArrayList<>();
            List<Question> mediumQuestions = new ArrayList<>();
            List<Question> hardQuestions = new ArrayList<>();

            // Handle different question sources
            if ("ai".equals(questionSource)) {
                // Pure AI - generate all questions with AI
                easyQuestions = generateAIQuestions(teacher, subject, grade, topics.get(i),
                        Question.Difficulty.EASY, easyCounts.get(i) * 2); // Generate more for selection
                mediumQuestions = generateAIQuestions(teacher, subject, grade, topics.get(i),
                        Question.Difficulty.MEDIUM, mediumCounts.get(i) * 2);
                hardQuestions = generateAIQuestions(teacher, subject, grade, topics.get(i),
                        Question.Difficulty.HARD, hardCounts.get(i) * 2);

            } else if ("mixed".equals(questionSource)) {
                // Mixed - try bank first, then AI if not enough
                easyQuestions = getQuestionsWithAIFallback(teacher, subject, grade, topics.get(i),
                        Question.Difficulty.EASY, easyCounts.get(i));
                mediumQuestions = getQuestionsWithAIFallback(teacher, subject, grade, topics.get(i),
                        Question.Difficulty.MEDIUM, mediumCounts.get(i));
                hardQuestions = getQuestionsWithAIFallback(teacher, subject, grade, topics.get(i),
                        Question.Difficulty.HARD, hardCounts.get(i));

            } else {
                // Bank only (default)
                easyQuestions = questionService.searchQuestions(
                        teacher, subject, grade, Question.Difficulty.EASY, topics.get(i));
                if (easyQuestions.isEmpty()) {
                    easyQuestions = questionService.searchQuestions(
                            teacher, subject, grade, Question.Difficulty.EASY, null);
                }

                mediumQuestions = questionService.searchQuestions(
                        teacher, subject, grade, Question.Difficulty.MEDIUM, topics.get(i));
                if (mediumQuestions.isEmpty()) {
                    mediumQuestions = questionService.searchQuestions(
                            teacher, subject, grade, Question.Difficulty.MEDIUM, null);
                }

                hardQuestions = questionService.searchQuestions(
                        teacher, subject, grade, Question.Difficulty.HARD, topics.get(i));
                if (hardQuestions.isEmpty()) {
                    hardQuestions = questionService.searchQuestions(
                            teacher, subject, grade, Question.Difficulty.HARD, null);
                }
            }

            section.put("easyQuestions", easyQuestions);
            section.put("mediumQuestions", mediumQuestions);
            section.put("hardQuestions", hardQuestions);

            matrixSections.add(section);
        }

        model.addAttribute("title", title);
        model.addAttribute("subject", subject);
        model.addAttribute("grade", grade);
        model.addAttribute("duration", duration);
        model.addAttribute("questionSource", questionSource);
        model.addAttribute("matrixJson", matrixJson.toString());
        model.addAttribute("matrixSections", matrixSections);

        return "teacher/test-builder-step3";
    }

    @PostMapping("/tests/builder/create")
    public String testBuilderCreate(@RequestParam String title,
            @RequestParam String subject,
            @RequestParam String grade,
            @RequestParam Integer duration,
            @RequestParam String matrixJson,
            @RequestParam Map<String, String> allParams,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        User teacher = getCurrentUser(auth);

        // Check if user can create test
        if (!paymentService.canCreateTest(teacher)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Bạn đã hết lượt tạo đề thi trong tháng này. Vui lòng nâng cấp gói dịch vụ.");
            return "redirect:/teacher/tests";
        }

        // Create test
        Test test = new Test();
        test.setTeacher(teacher);
        test.setTitle(title);
        test.setSubject(subject);
        test.setGrade(grade);
        test.setDurationMinutes(duration);
        test.setTestMatrix(matrixJson);

        // Collect all selected question IDs
        List<Long> questionIds = new ArrayList<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("questions_")) {
                questionIds.add(Long.parseLong(entry.getValue()));
            }
        }

        // Calculate total points
        double totalPoints = 0;
        for (Long qId : questionIds) {
            Question q = questionService.getQuestionById(qId).orElse(null);
            if (q != null) {
                totalPoints += q.getPoints();
            }
        }
        test.setTotalPoints(totalPoints);

        // Save test
        Test savedTest = testService.createTest(test);

        // Track test creation
        paymentService.trackTestCreation(teacher);

        // Add questions to test
        int orderNumber = 1;
        for (Long qId : questionIds) {
            Question q = questionService.getQuestionById(qId).orElse(null);
            if (q != null) {
                TestQuestion tq = new TestQuestion();
                tq.setTest(savedTest);
                tq.setQuestion(q);
                tq.setOrderNumber(orderNumber++);
                tq.setCustomPoints(q.getPoints());
                savedTest.getTestQuestions().add(tq);
            }
        }

        testService.updateTest(savedTest.getId(), savedTest);

        return "redirect:/teacher/tests/" + savedTest.getId();
    }

    // ========== QUẢN LÝ PHÒNG HỌC ==========

    @GetMapping("/classrooms")
    public String classrooms(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<Classroom> classrooms = classroomService.findByTeacherAndIsActive(teacher, true);

        // Đếm số học sinh cho mỗi phòng
        Map<Long, Long> memberCounts = new HashMap<>();
        for (Classroom classroom : classrooms) {
            long memberCount = classroomMemberService.countByClassroomAndStatus(
                    classroom, ClassroomMember.Status.ACTIVE);
            memberCounts.put(classroom.getId(), memberCount);
        }

        model.addAttribute("teacher", teacher);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("memberCounts", memberCounts);

        return "teacher/classrooms";
    }

    @GetMapping("/classrooms/new")
    public String newClassroom(Model model) {
        model.addAttribute("classroom", new Classroom());
        return "teacher/classroom-form";
    }

    @PostMapping("/classrooms")
    public String createClassroom(@ModelAttribute Classroom classroom, Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);

        // Kiểm tra giới hạn số lớp theo gói
        Plan currentPlan = teacher.getCurrentPlan();
        if (currentPlan != null) {
            long classroomCount = classroomService.countByTeacher(teacher);
            if (classroomCount >= currentPlan.getMaxClasses()) {
                model.addAttribute("error", "Bạn đã đạt giới hạn số lớp của gói " + currentPlan.getName());
                model.addAttribute("classroom", classroom);
                return "teacher/classroom-form";
            }
        }

        // Tạo roomId tự động nếu chưa có
        if (classroom.getRoomId() == null || classroom.getRoomId().isEmpty()) {
            classroom.setRoomId(generateRoomId());
        }

        // Kiểm tra roomId trùng
        if (classroomService.existsByRoomId(classroom.getRoomId())) {
            model.addAttribute("error", "Mã phòng đã tồn tại, vui lòng chọn mã khác");
            model.addAttribute("classroom", classroom);
            return "teacher/classroom-form";
        }

        classroom.setTeacher(teacher);
        classroom.setIsActive(true);
        classroomService.saveClassroom(classroom);

        return "redirect:/teacher/classrooms";
    }

    @GetMapping("/classrooms/{id}")
    public String viewClassroom(@PathVariable Long id, Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        Classroom classroom = classroomService.getClassroomById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/teacher/classrooms";
        }

        List<ClassroomMember> members = classroomMemberService.findByClassroomAndStatus(
                classroom, ClassroomMember.Status.ACTIVE);

        model.addAttribute("classroom", classroom);
        model.addAttribute("members", members);
        model.addAttribute("memberCount", members.size());

        // Kiểm tra giới hạn
        Plan currentPlan = teacher.getCurrentPlan();
        if (currentPlan != null) {
            model.addAttribute("maxStudents", currentPlan.getMaxStudents());
            model.addAttribute("canAddMore", members.size() < currentPlan.getMaxStudents());
        } else {
            model.addAttribute("maxStudents", null);
            model.addAttribute("canAddMore", true);
        }

        return "teacher/classroom-detail";
    }

    @GetMapping("/classrooms/{id}/edit")
    public String editClassroom(@PathVariable Long id, Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        Classroom classroom = classroomService.getClassroomById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/teacher/classrooms";
        }

        model.addAttribute("classroom", classroom);
        return "teacher/classroom-form";
    }

    @PostMapping("/classrooms/{id}")
    public String updateClassroom(@PathVariable Long id, @ModelAttribute Classroom classroomUpdate,
            Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        Classroom classroom = classroomService.getClassroomById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/teacher/classrooms";
        }

        // Kiểm tra roomId trùng (nếu thay đổi)
        if (!classroom.getRoomId().equals(classroomUpdate.getRoomId())) {
            if (classroomService.existsByRoomId(classroomUpdate.getRoomId())) {
                model.addAttribute("error", "Mã phòng đã tồn tại");
                model.addAttribute("classroom", classroomUpdate);
                return "teacher/classroom-form";
            }
            classroom.setRoomId(classroomUpdate.getRoomId());
        }

        classroom.setName(classroomUpdate.getName());
        classroom.setDescription(classroomUpdate.getDescription());
        classroom.setSubject(classroomUpdate.getSubject());
        classroom.setGrade(classroomUpdate.getGrade());
        classroom.setUpdatedAt(LocalDateTime.now());
        classroomService.saveClassroom(classroom);

        return "redirect:/teacher/classrooms/" + id;
    }

    @PostMapping("/classrooms/{classroomId}/students/{studentId}/remove")
    public String removeStudentFromClassroom(@PathVariable Long classroomId,
            @PathVariable Long studentId,
            Authentication auth) {
        User teacher = getCurrentUser(auth);
        Classroom classroom = classroomService.getClassroomById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            return "redirect:/teacher/classrooms";
        }

        User student = userService.getUserById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Optional<ClassroomMember> memberOpt = classroomMemberService.getMember(classroom, student);
        if (memberOpt.isPresent()) {
            ClassroomMember member = memberOpt.get();
            member.setStatus(ClassroomMember.Status.REMOVED);
            classroomMemberService.saveMember(member);
        }

        return "redirect:/teacher/classrooms/" + classroomId;
    }

    private String generateRoomId() {
        // Tạo mã phòng ngẫu nhiên (TC + 5 số)
        String roomId;
        do {
            roomId = "TC" + String.format("%05d", (int) (Math.random() * 100000));
        } while (classroomService.existsByRoomId(roomId));
        return roomId;
    }

    // Helper method to generate AI questions
    private List<Question> generateAIQuestions(User teacher, String subject, String grade,
            String topic, Question.Difficulty difficulty, int count) {
        if (count <= 0)
            return new ArrayList<>();

        try {
            List<Map<String, Object>> aiQuestions = aiService.generateQuestions(
                    subject, grade, topic,
                    difficulty == Question.Difficulty.EASY ? count : 0,
                    difficulty == Question.Difficulty.MEDIUM ? count : 0,
                    difficulty == Question.Difficulty.HARD ? count : 0,
                    teacher);

            List<Question> questions = new ArrayList<>();
            for (Map<String, Object> qData : aiQuestions) {
                Question q = new Question();
                q.setTeacher(teacher);
                q.setContent((String) qData.get("content"));
                q.setCorrectAnswer((String) qData.get("correctAnswer"));
                q.setExplanation((String) qData.get("explanation"));
                q.setDifficulty(Question.Difficulty.valueOf((String) qData.get("difficulty")));
                q.setSubject(subject);
                q.setGrade(grade);
                q.setTopic(topic);
                q.setQuestionType(Question.QuestionType.MULTIPLE_CHOICE);
                q.setCreatedBy(Question.CreatedBy.AI);
                q.setPoints(((Number) qData.get("points")).doubleValue());

                if (qData.containsKey("optionA"))
                    q.setOptionA((String) qData.get("optionA"));
                if (qData.containsKey("optionB"))
                    q.setOptionB((String) qData.get("optionB"));
                if (qData.containsKey("optionC"))
                    q.setOptionC((String) qData.get("optionC"));
                if (qData.containsKey("optionD"))
                    q.setOptionD((String) qData.get("optionD"));

                // Save to database
                Question saved = questionService.createQuestion(q);
                questions.add(saved);
            }
            return questions;
        } catch (Exception e) {
            System.err.println("Error generating AI questions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Helper method to get questions with AI fallback
    private List<Question> getQuestionsWithAIFallback(User teacher, String subject, String grade,
            String topic, Question.Difficulty difficulty, int needed) {
        System.out.println("=== Mixed mode: Getting " + needed + " " + difficulty + " questions for topic: " + topic);

        // Try to get from bank first
        List<Question> bankQuestions = questionService.searchQuestions(teacher, subject, grade, difficulty, topic);
        if (bankQuestions.isEmpty()) {
            bankQuestions = questionService.searchQuestions(teacher, subject, grade, difficulty, null);
        }

        System.out.println("=== Found " + bankQuestions.size() + " questions from bank");

        // If we have enough, return them
        if (bankQuestions.size() >= needed * 2) {
            System.out.println("=== Enough from bank, returning");
            return bankQuestions;
        }

        // Otherwise, generate more with AI
        int aiNeeded = Math.max(needed * 2 - bankQuestions.size(), needed);
        System.out.println("=== Need " + aiNeeded + " more from AI");

        List<Question> aiQuestions = generateAIQuestions(teacher, subject, grade, topic, difficulty, aiNeeded);
        System.out.println("=== Generated " + aiQuestions.size() + " questions with AI");

        // Combine both
        List<Question> combined = new ArrayList<>(bankQuestions);
        combined.addAll(aiQuestions);
        System.out.println("=== Total combined: " + combined.size() + " questions");
        return combined;
    }

    // ========== QUẢN LÝ HỌC SINH (Legacy - redirect to classrooms) ==========

    @GetMapping("/students")
    public String students(Authentication auth) {
        return "redirect:/teacher/classrooms";
    }

    // ========== PROFILE ==========

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        Subscription activeSubscription = paymentService.getActiveSubscription(teacher);
        model.addAttribute("user", teacher);
        model.addAttribute("activeSubscription", activeSubscription);
        return "teacher/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User userUpdate, Authentication auth) {
        User teacher = getCurrentUser(auth);
        teacher.setFullName(userUpdate.getFullName());
        teacher.setPhone(userUpdate.getPhone());
        teacher.setBio(userUpdate.getBio());
        userService.updateUser(teacher.getId(), teacher);
        return "redirect:/teacher/profile";
    }

    // ========== SUBSCRIPTION ==========

    @GetMapping("/subscription")
    public String subscription(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<Plan> plans = subscriptionService.getAllPlans();

        // Get active subscription with usage stats
        Subscription activeSubscription = paymentService.getActiveSubscription(teacher);

        model.addAttribute("teacher", teacher);
        model.addAttribute("plans", plans);
        model.addAttribute("activeSubscription", activeSubscription);
        return "teacher/subscription";
    }

    @PostMapping("/subscription/upgrade")
    public String upgradePlan(@RequestParam Long planId,
            @RequestParam(defaultValue = "1") int months,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {
        User teacher = getCurrentUser(auth);
        Plan plan = subscriptionService.getPlanById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        // Nếu là gói miễn phí (price = 0), activate trực tiếp không cần PayOS
        if (plan.getPrice() == 0) {
            try {
                // Cancel any existing active subscriptions
                List<Subscription> activeSubscriptions = subscriptionService.getActiveSubscriptions(teacher);

                for (Subscription activeSub : activeSubscriptions) {
                    activeSub.setStatus(Subscription.Status.CANCELLED);
                    subscriptionService.saveSubscription(activeSub);
                }

                // Create free subscription (vĩnh viễn)
                Subscription subscription = new Subscription();
                subscription.setUser(teacher);
                subscription.setPlan(plan);
                subscription.setStatus(Subscription.Status.ACTIVE);
                subscription.setPaymentStatus(Subscription.PaymentStatus.PAID);
                subscription.setStartDate(LocalDateTime.now());
                subscription.setEndDate(LocalDateTime.now().plusYears(100)); // Vĩnh viễn
                subscription.setAmount(0.0);
                subscription.setPaymentMethod("FREE");
                subscription.setTransactionId("FREE-" + System.currentTimeMillis());
                subscriptionService.saveSubscription(subscription);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã kích hoạt gói miễn phí thành công!");

                return "redirect:/teacher/subscription";

            } catch (Exception e) {
                log.error("Error activating free plan", e);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Có lỗi xảy ra khi kích hoạt gói miễn phí.");
                return "redirect:/teacher/subscription";
            }
        }

        // Gói trả phí - dùng PayOS
        try {
            // Create payment link with PayOS
            Map<String, Object> paymentData = paymentService.createPaymentLink(teacher, plan, months);

            // PayOS may return different field names, check both
            String checkoutUrl = null;
            if (paymentData.containsKey("checkoutUrl")) {
                checkoutUrl = (String) paymentData.get("checkoutUrl");
            } else if (paymentData.containsKey("checkout_url")) {
                checkoutUrl = (String) paymentData.get("checkout_url");
            } else if (paymentData.containsKey("paymentUrl")) {
                checkoutUrl = (String) paymentData.get("paymentUrl");
            } else if (paymentData.containsKey("payment_url")) {
                checkoutUrl = (String) paymentData.get("payment_url");
            }

            if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                log.error("No checkout URL in PayOS response. Available keys: {}", paymentData.keySet());
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không tìm thấy link thanh toán trong response từ PayOS");
                return "redirect:/teacher/subscription";
            }

            // Redirect to PayOS payment page
            return "redirect:" + checkoutUrl;

        } catch (Exception e) {
            log.error("Error in upgradePlan", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể tạo link thanh toán: " + e.getMessage());
            return "redirect:/teacher/subscription";
        }
    }

    // ========== LỊCH SỬ AI ==========

    @GetMapping("/ai-history")
    public String aiHistory(Authentication auth, Model model) {
        User teacher = getCurrentUser(auth);
        List<AIHistory> histories = aiService.getHistoryByTeacher(teacher);
        model.addAttribute("teacher", teacher);
        model.addAttribute("histories", histories);
        return "teacher/ai-history";
    }
}
