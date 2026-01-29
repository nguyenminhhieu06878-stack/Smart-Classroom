package com.smartclass.controller;

import com.smartclass.model.Classroom;
import com.smartclass.model.ClassroomMember;
import com.smartclass.model.User;
import com.smartclass.repository.ClassroomMemberRepository;
import com.smartclass.repository.ClassroomRepository;
import com.smartclass.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final UserService userService;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMemberRepository classroomMemberRepository;
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@RequestParam String role,
                          @RequestParam String fullName,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          @RequestParam(required = false) String classroomCode,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            // Validate password match
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp");
                return "register";
            }
            
            // Check if email exists
            if (userService.existsByEmail(email)) {
                model.addAttribute("error", "Email đã được sử dụng");
                return "register";
            }
            
            // Create user
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setFullName(fullName);
            user.setRole(User.Role.valueOf(role));
            user.setActive(true);
            
            User savedUser = userService.createUser(user);
            
            // If student and has classroom code, auto-join
            if (role.equals("STUDENT") && classroomCode != null && !classroomCode.trim().isEmpty()) {
                Classroom classroom = classroomRepository.findByRoomId(classroomCode.trim()).orElse(null);
                
                if (classroom != null && classroom.getIsActive()) {
                    // Check if already a member
                    boolean isMember = classroomMemberRepository
                        .existsByClassroomAndStudent(classroom, savedUser);
                    
                    if (!isMember) {
                        ClassroomMember member = new ClassroomMember();
                        member.setClassroom(classroom);
                        member.setStudent(savedUser);
                        member.setJoinedAt(LocalDateTime.now());
                        classroomMemberRepository.save(member);
                        
                        redirectAttributes.addFlashAttribute("success", 
                            "Đăng ký thành công! Bạn đã tham gia lớp " + classroom.getName());
                    } else {
                        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("success", 
                        "Đăng ký thành công! (Mã phòng học không hợp lệ)");
                }
            } else {
                redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            }
            
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("TEACHER"))) {
            return "redirect:/teacher/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("STUDENT"))) {
            return "redirect:/student/dashboard";
        }
        return "redirect:/";
    }
}
