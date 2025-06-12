package com.uni.ethesis.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true
)
@AllArgsConstructor
public class SecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = "http://localhost:8080/realms/graduation-spring-realm";
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakAuthorityConverter());
        return jwtAuthenticationConverter;
    }

    @Bean
    GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return new KeycloakGrantedAuthoritiesMapper();
    }

//    @Bean
//    AuthenticationSuccessHandler userAuthenticationSuccessHandler() {
//        return new UserSynchronizationSuccessHandler();
//    }

//    @Bean
//    public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
//        return new KeycloakGrantedAuthoritiesMapper();
//    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/public/**" , "/css/**" , "/js/**").permitAll() // Permit access to public resources
                        .requestMatchers("/dashboard/**").authenticated()
                        .anyRequest().permitAll()
                ).oauth2ResourceServer(auth ->
                        auth.jwt(jwtConfigurer ->
                                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .oauth2Login(login ->
                        login.userInfoEndpoint(userInfo ->
                                userInfo.userAuthoritiesMapper(userAuthoritiesMapper())
                        )
                ).oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
