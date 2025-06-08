package com.uni.ethesis;

import org.springframework.boot.SpringApplication;

public class TestEThesisApplication {

    public static void main(String[] args) {
        SpringApplication.from(EThesisApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
