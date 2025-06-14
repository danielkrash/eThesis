package com.uni.ethesis;

import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootApplication
public class EThesisApplication {

    public static void main(String[] args) {
        SpringApplication.run(EThesisApplication.class, args);
    }

}
