package com.uni.ethesis.web.view.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uni.ethesis.exceptions.FileUploadException;
import com.uni.ethesis.exceptions.InvalidStatusTransitionException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.exceptions.UnauthorizedCommentException;

/**
 * Test controller to verify exception handling works correctly
 * This should be removed in production
 */
@RestController
@RequestMapping("/test/exceptions")
public class ExceptionTestController {
    
    @GetMapping("/404")
    public String test404() {
        throw new TeacherNotFoundException("Test teacher not found");
    }
    
    @GetMapping("/403")
    public String test403() {
        throw new UnauthorizedCommentException("Test access denied");
    }
    
    @GetMapping("/400")
    public String test400(@RequestParam UUID id) {
        // This will throw a conversion error if UUID is invalid
        return "ID: " + id;
    }
    
    @GetMapping("/409")
    public String test409() {
        throw new InvalidStatusTransitionException("Test conflict");
    }
    
    @GetMapping("/422")
    public String test422() {
        throw new FileUploadException("Test file upload error");
    }
    
    @GetMapping("/500")
    public String test500() {
        throw new RuntimeException("Test internal server error");
    }
    
    @GetMapping("/auth-required")
    @PreAuthorize("hasRole('ADMIN')")
    public String testAuthRequired() {
        return "This should require authentication";
    }
}
