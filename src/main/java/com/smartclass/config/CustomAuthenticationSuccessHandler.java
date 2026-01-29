package com.smartclass.config;

import com.smartclass.model.Classroom;
import com.smartclass.model.ClassroomMember;
import com.smartclass.model.User;
import com.smartclass.service.ClassroomMemberService;
import com.smartclass.service.ClassroomService;
import com.smartclass.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final ClassroomService classroomService;
    private final ClassroomMemberService classroomMemberService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String classroomCode = request.getParameter("classroomCode");

        // If classroom code is provided and user is a student, auto-join
        if (classroomCode != null && !classroomCode.trim().isEmpty()) {
            String username = authentication.getName();
            User user = userService.getUserByEmail(username).orElse(null);

            if (user != null && user.getRole() == User.Role.STUDENT) {
                Classroom classroom = classroomService.getClassroomByRoomId(classroomCode.trim()).orElse(null);

                if (classroom != null && classroom.getIsActive()) {
                    // Check if already a member
                    boolean isMember = classroomMemberService.isMember(classroom, user);

                    if (!isMember) {
                        ClassroomMember member = new ClassroomMember();
                        member.setClassroom(classroom);
                        member.setStudent(user);
                        member.setJoinedAt(LocalDateTime.now());
                        classroomMemberService.saveMember(member);
                    }
                }
            }
        }

        // Redirect based on role
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            response.sendRedirect("/admin/dashboard");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("TEACHER"))) {
            response.sendRedirect("/teacher/dashboard");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("STUDENT"))) {
            response.sendRedirect("/student/dashboard");
        } else {
            response.sendRedirect("/");
        }
    }
}
