package com.uni.ethesis.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.uni.ethesis.exceptions.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle specific exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", ex.getMessage());
        body.put("details", request.getDescription(false));

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        } else {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("timestamp", LocalDateTime.now().toString());
            modelAndView.addObject("message", ex.getMessage());
            modelAndView.addObject("details", request.getDescription(false));
            modelAndView.addObject("status", HttpStatus.NOT_FOUND.value());
            modelAndView.addObject("error", HttpStatus.NOT_FOUND.getReasonPhrase());
            modelAndView.setViewName("error");
            return modelAndView;
        }
    }

    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public Object handleGlobalException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message", "An unexpected error occurred. Please contact support.");
        body.put("details", request.getDescription(false));
        // It's good practice not to expose raw exception messages to the client in production for generic errors.
        // Log the full exception for debugging purposes.
        // logger.error("Unhandled exception:", ex);

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            // For API, return JSON
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // For MVC, return an error page
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("timestamp", LocalDateTime.now().toString());
            modelAndView.addObject("message", "An unexpected error occurred. Please contact support.");
            modelAndView.addObject("details", request.getDescription(false));
            modelAndView.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            modelAndView.addObject("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            // Optionally, you might want to add ex.getMessage() for non-production environments
            // modelAndView.addObject("actualError", ex.getMessage()); 
            modelAndView.setViewName("error");
            return modelAndView;
        }
    }
}
