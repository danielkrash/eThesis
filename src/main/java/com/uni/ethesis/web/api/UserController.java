package com.uni.ethesis.web.api;

import com.uni.ethesis.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class UserController {
    private final UserService userService;
    @GetMapping
    public String test() {
        userService.deleteUserById(UUID.fromString("4cda65cf-bac7-4a0f-ba11-f42cdb713da7"));
        return "Test successful!";
    }
}
