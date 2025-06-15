package com.uni.ethesis.web.view.controller;

import java.time.LocalDateTime;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexController {
    @GetMapping("/")
    public String showIndex(Model model, Authentication auth) {
        // If user is authenticated, redirect to dashboard
        if(auth != null && auth.isAuthenticated()){
            return "redirect:/dashboard";
        }
        
        // If not authenticated, show the public index page
        model.addAttribute("title", "University Thesis Portal");
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