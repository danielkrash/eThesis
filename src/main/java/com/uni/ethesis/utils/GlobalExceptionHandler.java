package com.uni.ethesis.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.uni.ethesis.exceptions.AppointmentConflictException;
import com.uni.ethesis.exceptions.CommentNotFoundException;
import com.uni.ethesis.exceptions.DefenseNotFoundException;
import com.uni.ethesis.exceptions.DefenseSessionNotFoundException;
import com.uni.ethesis.exceptions.DefenseSessionProfessorNotFoundException;
import com.uni.ethesis.exceptions.DepartmentAppointmentNotFoundException;
import com.uni.ethesis.exceptions.DepartmentNotFoundException;
import com.uni.ethesis.exceptions.FileUploadException;
import com.uni.ethesis.exceptions.InvalidStatusTransitionException;
import com.uni.ethesis.exceptions.ProfessorAlreadyAssignedException;
import com.uni.ethesis.exceptions.ResourceNotFoundException;
import com.uni.ethesis.exceptions.ReviewNotFoundException;
import com.uni.ethesis.exceptions.ServiceException;
import com.uni.ethesis.exceptions.StudentNotFoundException;
import com.uni.ethesis.exceptions.TeacherNotFoundException;
import com.uni.ethesis.exceptions.ThesisNotFoundException;
import com.uni.ethesis.exceptions.ThesisProposalNotFoundException;
import com.uni.ethesis.exceptions.UnauthorizedCommentException;
import com.uni.ethesis.exceptions.UserNotFoundException;

/**
 * Global Exception Handler for the eThesis application.
 * 
 * This controller advice handles all exceptions thrown in the application and maps them
 * to appropriate HTTP status codes:
 * 
 * - 404 NOT FOUND: Resource not found exceptions (UserNotFoundException, etc.)
 * - 403 FORBIDDEN: Access denied exceptions (AccessDeniedException, etc.)
 * - 401 UNAUTHORIZED: Authentication required exceptions
 * - 400 BAD REQUEST: Invalid request parameters
 * - 405 METHOD NOT ALLOWED: HTTP method not supported
 * - 409 CONFLICT: Resource conflicts (AppointmentConflictException, etc.)
 * - 422 UNPROCESSABLE ENTITY: Request cannot be processed (FileUploadException, etc.)
 * - 500 INTERNAL SERVER ERROR: Unexpected server errors
 * 
 * Each exception generates a unique error ID for tracking and logs appropriate
 * information based on the error severity.
 * 
 * @author System
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Value("${app.debug.detailed-errors:false}")
    private boolean showDetailedErrors;

    // ====== 404 NOT FOUND EXCEPTIONS ======
    
    @ExceptionHandler({
        ResourceNotFoundException.class,
        UserNotFoundException.class,
        StudentNotFoundException.class,
        TeacherNotFoundException.class,
        DepartmentNotFoundException.class,
        ThesisNotFoundException.class,
        ThesisProposalNotFoundException.class,
        DefenseNotFoundException.class,
        DefenseSessionNotFoundException.class,
        DefenseSessionProfessorNotFoundException.class,
        DepartmentAppointmentNotFoundException.class,
        ReviewNotFoundException.class,
        CommentNotFoundException.class,
        NoHandlerFoundException.class
    })
    public Object handleNotFoundExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.NOT_FOUND, "The requested resource was not found");
    }

    // ====== 403 FORBIDDEN / UNAUTHORIZED EXCEPTIONS ======
    
    @ExceptionHandler({
        AccessDeniedException.class,
        UnauthorizedCommentException.class
    })
    public Object handleForbiddenExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.FORBIDDEN, "Access denied");
    }

    // ====== 401 UNAUTHORIZED EXCEPTIONS ======
    
    @ExceptionHandler({
        AuthenticationException.class,
        BadCredentialsException.class
    })
    public Object handleUnauthorizedExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    // ====== 400 BAD REQUEST EXCEPTIONS ======
    
    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        IllegalArgumentException.class
    })
    public Object handleBadRequestExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.BAD_REQUEST, "Invalid request parameters");
    }

    // ====== 405 METHOD NOT ALLOWED EXCEPTIONS ======
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotAllowedExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported");
    }

    // ====== 409 CONFLICT EXCEPTIONS ======
    
    @ExceptionHandler({
        AppointmentConflictException.class,
        ProfessorAlreadyAssignedException.class,
        InvalidStatusTransitionException.class
    })
    public Object handleConflictExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.CONFLICT, "Resource conflict");
    }

    // ====== 422 UNPROCESSABLE ENTITY EXCEPTIONS ======
    
    @ExceptionHandler({
        FileUploadException.class
    })
    public Object handleUnprocessableEntityExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.UNPROCESSABLE_ENTITY, "Request cannot be processed");
    }

    // ====== 500 INTERNAL SERVER ERROR EXCEPTIONS ======
    
    @ExceptionHandler({
        ServiceException.class,
        RuntimeException.class,
        Exception.class
    })
    public Object handleInternalServerErrorExceptions(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // ====== HELPER METHOD TO BUILD ERROR RESPONSES ======
    
    private Object buildErrorResponse(Exception ex, WebRequest request, HttpStatus status, String defaultMessage) {
        // Generate unique error ID for tracking
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        
        // Log the exception details for debugging
        if (status.is5xxServerError()) {
            logger.error("Server error [Error ID: {}] at {}: {}", 
                        errorId, request.getDescription(false), ex.getMessage(), ex);
        } else if (status.is4xxClientError()) {
            logger.warn("Client error [Error ID: {}] at {}: {}", 
                        errorId, request.getDescription(false), ex.getMessage());
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("errorId", errorId);
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("path", extractPath(request));
        
        // Determine message to show
        String message;
        if (showDetailedErrors && ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
            message = ex.getMessage();
        } else {
            message = defaultMessage + ". Error ID: " + errorId;
        }
        body.put("message", message);
        
        // Include detailed error information if enabled
        if (showDetailedErrors) {
            body.put("exceptionType", ex.getClass().getSimpleName());
            if (ex.getCause() != null) {
                body.put("rootCause", ex.getCause().getMessage());
            }
        }

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            // For API, return JSON
            return new ResponseEntity<>(body, status);
        } else {
            // For MVC, return an error page
            ModelAndView modelAndView = new ModelAndView();
            body.forEach(modelAndView::addObject);
            modelAndView.setViewName("error");
            return modelAndView;
        }
    }
    
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }
}
