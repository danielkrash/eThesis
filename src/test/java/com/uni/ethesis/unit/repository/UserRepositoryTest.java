package com.uni.ethesis.unit.repository;

import com.uni.ethesis.data.entities.Department;
import com.uni.ethesis.data.entities.User;
import com.uni.ethesis.data.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
public class UserTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1 , user2 , user3;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Ensure a clean state before each test, though @DataJpaTest rolls back

        user1 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        user2 = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .build();
        user3 = User.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .build();

        var t = postgres.getUsername();
        var p = postgres.getPassword();
        var s = postgres.getDatabaseName();
        entityManager.persist(user1);
        entityManager.flush(); // Flush after persisting user1

        entityManager.persist(user2);
        entityManager.flush(); // Flush after persisting user2

        entityManager.persist(user3);
        entityManager.flush(); // Original flush, now after persisting user3
    }

    @Test
    public void test1() {
        User u1 = userRepository.findById(user1.getId()).orElse(null);
        User newUser = User.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("test@mail.com").build();
        assertThat(u1).isNotNull();
        userRepository.save(newUser);
        var savedUser = userRepository.findById(newUser.getId());
        userRepository.deleteById(newUser.getId());
        var deletedUser = userRepository.findById(newUser.getId());
    }
}
