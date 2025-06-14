package com.uni.ethesis.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.uni.ethesis.data.dto.LoginRequest;
import com.uni.ethesis.data.dto.LoginResponse;
import com.uni.ethesis.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${spring.security.oauth2.client.provider.graduation-application.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.graduation-application.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.graduation-application.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.graduation-application.authorization-grant-type}")
    private String grantType;

    @Value("${spring.security.oauth2.client.registration.graduation-application.scope}")
    private String scope;

    private final RestTemplate restTemplate;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", grantType);
        map.add("scope", scope);
        map.add("username", loginRequest.getUsername());
        map.add("password", loginRequest.getPassword());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        try {
            // Log the request body for debugging purposes
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(tokenUri, entity, LoginResponse.class);
            System.out.println("Request Body: " + entity.getBody());
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error logging request body: " + e.getMessage());
        }
        return null;
    }
}
