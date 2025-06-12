package com.uni.ethesis;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestEThesisApplication {

    public static void main(String[] args) {
        SpringApplication.from(EThesisApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
