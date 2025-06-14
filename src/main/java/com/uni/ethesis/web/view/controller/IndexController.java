package com.uni.ethesis.web.view.controller;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.uni.ethesis.web.view.model.UserViewModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexController {
    @GetMapping("/")
    public String showIndex(Model model, Authentication auth) {
        if(auth != null){
            var roleNames = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            var user = new UserViewModel();
            if(roleNames.contains("ROLE_student")){
                user.setRole("student");
            }else if(roleNames.contains("ROLE_teacher")) {
                user.setRole("teacher");
            }
            if (auth.getPrincipal() instanceof OAuth2User principal) { // Check if principal is OAuth2 related
                var attributes =  principal.getAttributes(); // Get attributes map
                user.setFirstName(attributes.get("given_name").toString());
                user.setLastName(attributes.get("family_name").toString());
                user.setEmail(attributes.get("email").toString());
                model.addAttribute("user", user); // Use attributes map
            }
            if(auth.getPrincipal() instanceof Jwt jwt){
                var attributes =  jwt.getClaims(); // Get attributes map
                user.setFirstName(attributes.get("given_name").toString());
                user.setLastName(attributes.get("family_name").toString());
                user.setEmail(attributes.get("email").toString());
                model.addAttribute("user", user);
            }
//            var roleNames = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
//            model.addAttribute("roleNames", roleNames);
//            if (auth.getPrincipal() instanceof OAuth2User principal) { // Check if principal is OAuth2 related
//                var attributes =  principal.getAttributes(); // Get attributes map
//                model.addAttribute("userInfo", attributes); // Use attributes map
//            }
//            if(auth.getPrincipal() instanceof Jwt jwt){
//                model.addAttribute("userInfo", jwt.getClaims());
//            }
//            var userId = auth.getName();
//            model.addAttribute("userId", userId);
        }
        return "index";
    }

    @PostMapping("/clicked")
    public String clicked(Model model) {
        log.info("clicked button");
        model.addAttribute("now", LocalDateTime.now().toString());
        return "fragments/clicked :: result";
    }

    @GetMapping("/test-error")
    public String testError() {
        throw new RuntimeException("This is a test exception to demonstrate the error page.");
    }
}