package com.uni.ethesis.unit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.uni.ethesis.data.repo.UserRepository;
import com.uni.ethesis.unit.repository.config.TestDataPopulator;

@Testcontainers
@DataJpaTest
public class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private TestDataPopulator testDataPopulator;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Ensure a clean state before each test, though @DataJpaTest rolls back
        testDataPopulator = new TestDataPopulator();
        testDataPopulator.populate(entityManager);
    }

    @Test
    public void test1() {
        var user1 = userRepository.findUserById(testDataPopulator.getUserAlice().getId());
        userRepository.deleteById(user1.getId());
        var deletedUser = userRepository.findById(user1.getId());
        assertThat(deletedUser).isEmpty(); // Dummy assertion to ensure the test runs
    }
}
