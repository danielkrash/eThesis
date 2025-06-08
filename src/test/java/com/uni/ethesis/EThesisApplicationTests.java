package com.uni.ethesis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EThesisApplicationTests {

    @Test
    void contextLoads() {
    }

}
