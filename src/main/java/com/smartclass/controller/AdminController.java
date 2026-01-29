package com.smartclass.controller;

import com.smartclass.model.*;
import com.smartclass.repository.*;
import com.smartclass.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    private final PlanRepository planRepository;
    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final TestRepository testRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // User stats
            long totalUsers = userService.getAllUsers().size();
            long teachers = userService.getUsersByRole(User.Role.TEACHER).size();
            long students = userService.getUsersByRole(User.Role.STUDENT).size();
            
            // Content stats
            long totalQuestions = questionRepository.count();
            long totalLessons = lessonRepository.count();
            long totalTests = testRepository.count();
            
            // Subscription stats
            long activeSubscriptions = subscriptionRepository.countByStatus(Subscription.Status.ACTIVE);
            
            // Revenue stats (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            Double monthlyRevenue = subscriptionRepository.getTotalRevenue(thirtyDaysAgo);
            if (monthlyRevenue == null) monthlyRevenue = 0.0;
            
            // Revenue chart data - simplified
            List<String> chartLabels = new ArrayList<>();
            List<Double> chartData = new ArrayList<>();
            
            // Recent subscriptions
            List<Subscription> recentSubscriptions = subscriptionRepository.findRecentSubscriptions(
                LocalDateTime.now().minusDays(7)
            );
            
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("teachers", teachers);
            model.addAttribute("students", students);
            model.addAttribute("totalQuestions", totalQuestions);
            model.addAttribute("totalLessons", totalLessons);
            model.addAttribute("totalTests", totalTests);
            model.addAttribute("activeSubscriptions", activeSubscriptions);
            model.addAttribute("monthlyRevenue", monthlyRevenue);
            model.addAttribute("chartLabels", chartLabels);
            model.addAttribute("chartData", chartData);
            model.addAttribute("recentSubscriptions", recentSubscriptions);
            
            return "admin/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "admin/dashboard";
        }
    }
    
    @GetMapping("/users")
    public String users(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }
    
    @GetMapping("/users/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        return "admin/user-form";
    }
    
    @PostMapping("/users")
    public String createUser(@ModelAttribute User user) {
        userService.createUser(user);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin/user-form";
    }
    
    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        userService.updateUser(id, user);
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
    
    @GetMapping("/plans")
    public String plans(Model model) {
        List<Plan> plans = planRepository.findAll();
        model.addAttribute("plans", plans);
        return "admin/plans";
    }
    
    @GetMapping("/payments")
    public String payments(Model model) {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        subscriptions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        // Calculate stats
        long paidCount = subscriptions.stream()
            .filter(s -> s.getPaymentStatus() == Subscription.PaymentStatus.PAID)
            .count();
        long pendingCount = subscriptions.stream()
            .filter(s -> s.getPaymentStatus() == Subscription.PaymentStatus.PENDING)
            .count();
        long failedCount = subscriptions.stream()
            .filter(s -> s.getPaymentStatus() == Subscription.PaymentStatus.FAILED)
            .count();
        double totalRevenue = subscriptions.stream()
            .filter(s -> s.getPaymentStatus() == Subscription.PaymentStatus.PAID)
            .mapToDouble(s -> s.getAmount() != null ? s.getAmount() : 0.0)
            .sum();
        
        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("totalRevenue", totalRevenue);
        
        return "admin/payments";
    }
    
    // ========== PROFILE ==========
    
    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        User admin = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", admin);
        return "admin/profile";
    }
    
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User userUpdate, Authentication auth) {
        User admin = userService.getUserByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        admin.setFullName(userUpdate.getFullName());
        admin.setPhone(userUpdate.getPhone());
        admin.setBio(userUpdate.getBio());
        userService.updateUser(admin.getId(), admin);
        return "redirect:/admin/profile";
    }
}
