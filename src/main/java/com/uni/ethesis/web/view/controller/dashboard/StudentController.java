package com.uni.ethesis.web.view.controller.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.web.view.model.UserViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsible for student management functionality.
 * This is separate from the dashboard to follow Single Responsibility Principle.
 */
@Slf4j
@Controller
@RequestMapping("/dashboard/students")
@RequiredArgsConstructor
public class StudentController {
    
    private final UserViewService userViewService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public String showStudents(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Unauthenticated user trying to access students");
            return "redirect:/";
        }

        try {
            // Get user information using UserViewService
            UserViewModel user = userViewService.getCurrentUserViewModel(auth);
            model.addAttribute("user", user);

            // For now, we'll just show a placeholder page
            // TODO: Implement student management functionality
            model.addAttribute("pageTitle", "My Students");
            model.addAttribute("pageDescription", "Manage students under your supervision");

            log.info("Students page loaded for teacher: {}", user.getEmail());
            return "dashboard/students/list";

        } catch (Exception e) {
            log.error("Error loading students page", e);
            model.addAttribute("error", "Failed to load students");
            return "dashboard/main";
        }
    }
}
