package com.uni.ethesis.web.view.controller;

import java.time.LocalDateTime;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.web.view.model.UserViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexController {
    
    private final UserViewService userViewService;
    @GetMapping("/")
    public String showIndex(Model model, Authentication auth) {
        model.addAttribute("title", "University Thesis Portal");
        
        // Add user information if authenticated
        if (auth != null && auth.isAuthenticated()) {
            try {
                UserViewModel user = userViewService.getCurrentUserViewModel(auth);
                model.addAttribute("user", user);
            } catch (Exception e) {
                log.warn("Could not fetch user information for authenticated user: {}", e.getMessage());
                // Continue without user info - template will handle gracefully
            }
        }
        
        return "index";
    }

    @PostMapping("/clicked")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public String clicked(Model model) {
        log.info("clicked button");
        model.addAttribute("now", LocalDateTime.now().toString());
        return "fragments/clicked :: result";
    }

    @GetMapping("/test-error")
    @PreAuthorize("hasRole('ADMIN')")
    public String testError() {
        throw new RuntimeException("This is a test exception to demonstrate the error page.");
    }
}