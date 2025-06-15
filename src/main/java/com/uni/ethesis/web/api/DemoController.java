package com.uni.ethesis.web.api;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public String hello() {
        return "Hello from Spring boot & Keycloak";
    }
    @GetMapping("/hello-2")
    @PreAuthorize("hasRole('ADMIN')")
    public String hello2() {
        return "Hello from Spring boot & Keycloak - ADMIN";
    }
    @GetMapping("/user-email")
    public String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt) {
            org.springframework.security.oauth2.jwt.Jwt jwt = (org.springframework.security.oauth2.jwt.Jwt) authentication.getPrincipal();

            // Extract the email claim
            String email = jwt.getClaim("email");
            return "Email: " + email;
        }

        return "User email not found";
    }
    @GetMapping("/user-info")
    public String getUserInfo(Authentication authentication) {
//        if(authentication instanceof JwtAuthenticationToken){
//            OAuth2User cu
//        }
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        DefaultOidcUser user = (DefaultOidcUser) authentication.getPrincipal();
        String username = user.getAttribute("preferred_username");
        List<GrantedAuthority> roles = (List<GrantedAuthority>) authentication.getAuthorities().stream().toList();
        return "Username: " + username + ", Roles: " + roles;
    }
}
