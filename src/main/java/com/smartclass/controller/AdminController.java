package com.smartclass.controller;

import com.smartclass.model.*;
import com.smartclass.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ========== DASHBOARD ==========

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = adminService.getDashboardStatistics();
        model.addAllAttributes(stats);
        return "admin/dashboard";
    }

    // ========== USERS MANAGEMENT ==========

    @GetMapping("/users")
    public String users(Model model) {
        List<User> users = adminService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            adminService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo ngườii dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        User user = adminService.getUserById(id);
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateUser(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ngườii dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa ngườii dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = adminService.toggleUserStatus(id);
            String status = user.getActive() ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("successMessage", "Đã " + status + " tài khoản " + user.getEmail());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        try {
            User user = adminService.getUserById(id);
            adminService.resetUserPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đã reset mật khẩu cho " + user.getEmail());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ========== PLANS MANAGEMENT ==========

    @GetMapping("/plans")
    public String plans(Model model) {
        List<Plan> plans = adminService.getAllPlans();
        model.addAttribute("plans", plans);
        return "admin/plans";
    }

    @GetMapping("/plans/new")
    public String newPlan(Model model) {
        model.addAttribute("plan", new Plan());
        return "admin/plan-form";
    }

    @PostMapping("/plans")
    public String createPlan(@ModelAttribute Plan plan, RedirectAttributes redirectAttributes) {
        try {
            adminService.createPlan(plan);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo gói dịch vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/plans";
    }

    @GetMapping("/plans/{id}/edit")
    public String editPlan(@PathVariable Long id, Model model) {
        Plan plan = adminService.getPlanById(id);
        model.addAttribute("plan", plan);
        return "admin/plan-form";
    }

    @PostMapping("/plans/{id}")
    public String updatePlan(@PathVariable Long id, @ModelAttribute Plan plan, RedirectAttributes redirectAttributes) {
        try {
            adminService.updatePlan(id, plan);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật gói dịch vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/plans";
    }

    @PostMapping("/plans/{id}/delete")
    public String deletePlan(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deletePlan(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa gói dịch vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/plans";
    }

    @PostMapping("/plans/{id}/toggle")
    public String togglePlanStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Plan plan = adminService.togglePlanStatus(id);
            String status = plan.getActive() ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("successMessage", "Đã " + status + " gói " + plan.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/plans";
    }

    // ========== PAYMENTS MANAGEMENT ==========

    @GetMapping("/payments")
    public String payments(Model model) {
        Map<String, Object> stats = adminService.getPaymentStatistics();
        model.addAllAttributes(stats);
        return "admin/payments";
    }

    @PostMapping("/payments/{id}/confirm")
    public String confirmPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.confirmPayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận thanh toán!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/payments";
    }

    @PostMapping("/payments/{id}/cancel")
    public String cancelPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.cancelPayment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy giao dịch!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/payments";
    }

    // ========== CONTENT MANAGEMENT - QUESTIONS ==========

    @GetMapping("/questions")
    public String questions(Model model,
                           @RequestParam(required = false) String subject,
                           @RequestParam(required = false) String grade,
                           @RequestParam(required = false) String keyword) {
        List<Question> questions = adminService.getAllQuestions(subject, grade, keyword);
        model.addAttribute("questions", questions);
        model.addAttribute("subject", subject);
        model.addAttribute("grade", grade);
        model.addAttribute("keyword", keyword);
        return "admin/questions";
    }

    @GetMapping("/questions/{id}")
    public String viewQuestion(@PathVariable Long id, Model model) {
        Question question = adminService.getQuestionById(id);
        model.addAttribute("question", question);
        return "admin/question-detail";
    }

    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa câu hỏi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/questions";
    }

    // ========== CONTENT MANAGEMENT - LESSONS ==========

    @GetMapping("/lessons")
    public String lessons(Model model,
                         @RequestParam(required = false) String subject,
                         @RequestParam(required = false) String grade) {
        List<Lesson> lessons = adminService.getAllLessons(subject, grade);
        model.addAttribute("lessons", lessons);
        model.addAttribute("subject", subject);
        model.addAttribute("grade", grade);
        return "admin/lessons";
    }

    @GetMapping("/lessons/{id}")
    public String viewLesson(@PathVariable Long id, Model model) {
        Lesson lesson = adminService.getLessonById(id);
        model.addAttribute("lesson", lesson);
        return "admin/lesson-detail";
    }

    @PostMapping("/lessons/{id}/delete")
    public String deleteLesson(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteLesson(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bài giảng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/lessons";
    }

    // ========== CONTENT MANAGEMENT - TESTS ==========

    @GetMapping("/tests")
    public String tests(Model model,
                       @RequestParam(required = false) String subject,
                       @RequestParam(required = false) String grade) {
        List<Test> tests = adminService.getAllTests(subject, grade);
        model.addAttribute("tests", tests);
        model.addAttribute("subject", subject);
        model.addAttribute("grade", grade);
        return "admin/tests";
    }

    @GetMapping("/tests/{id}")
    public String viewTest(@PathVariable Long id, Model model) {
        Test test = adminService.getTestById(id);
        model.addAttribute("test", test);
        return "admin/test-detail";
    }

    @PostMapping("/tests/{id}/delete")
    public String deleteTest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteTest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa đề thi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tests";
    }

    // ========== CONTENT MANAGEMENT - CLASSROOMS ==========

    @GetMapping("/classrooms")
    public String classrooms(Model model) {
        List<Classroom> classrooms = adminService.getAllClassrooms();
        Map<Long, Long> memberCounts = adminService.getClassroomMemberCounts(classrooms);
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("memberCounts", memberCounts);
        return "admin/classrooms";
    }

    @GetMapping("/classrooms/{id}")
    public String viewClassroom(@PathVariable Long id, Model model) {
        Classroom classroom = adminService.getClassroomById(id);
        List<ClassroomMember> members = adminService.getActiveClassroomMembers(classroom);
        model.addAttribute("classroom", classroom);
        model.addAttribute("members", members);
        model.addAttribute("memberCount", members.size());
        return "admin/classroom-detail";
    }

    @PostMapping("/classrooms/{id}/delete")
    public String deleteClassroom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteClassroom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa lớp học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/classrooms";
    }

    // ========== AI HISTORY ==========

    @GetMapping("/ai-history")
    public String aiHistory(Model model,
                           @RequestParam(required = false) String type,
                           @RequestParam(required = false) String keyword) {
        List<AIHistory> histories = adminService.getAllAIHistory(type, keyword);
        model.addAttribute("histories", histories);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        return "admin/ai-history";
    }

    @PostMapping("/ai-history/{id}/delete")
    public String deleteAIHistory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteAIHistory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa lịch sử AI thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/ai-history";
    }

    // ========== PROFILE ==========

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        User admin = adminService.getUserByEmail(auth.getName());
        model.addAttribute("user", admin);
        return "admin/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User userUpdate, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            adminService.updateProfile(auth.getName(), userUpdate);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }
}
