package com.uni.ethesis.unit.repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

import com.uni.ethesis.data.entities.User;
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
        // userRepository.deleteAll(); // Not strictly necessary with @DataJpaTest due to transaction rollback
        testDataPopulator = new TestDataPopulator();
        testDataPopulator.populate(entityManager);
    }

    @Test
    public void test1() { // Renamed for clarity, consider more descriptive names
        User userAliceFromPopulator = testDataPopulator.getUserAlice();
        Optional<User> user1 = userRepository.findUserById(userAliceFromPopulator.getId());
        assertThat(user1).isNotNull();
        assertThat(user1.get().getFirstName()).isEqualTo("Alice");
        assertThat(user1.get().getLastName()).isEqualTo("Wonderland"); // Added assertion for last name
        // userRepository.deleteById(user1.getId()); // Deleting here might affect other tests if not rolled back properly
        // var deletedUser = userRepository.findById(user1.getId());
        // assertThat(deletedUser).isEmpty();
    }

    @Test
    void findUsersByFirstNameAndLastName_shouldReturnMatchingUsers() {
        List<User> users = userRepository.findUsersByFirstNameAndLastName("Alice", "Wonderland");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("alice.student@example.com");
    }

    @Test
    void findUsersByFirstName_shouldReturnMatchingUsers() {
        List<User> users = userRepository.findUsersByFirstName("Bob");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getLastName()).isEqualTo("Builder");
    }

    @Test
    void findUsersByLastName_shouldReturnMatchingUsers() {
        List<User> users = userRepository.findUsersByLastName("Santana");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("Carlos");
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(7); // Alice, Bob, Carlos, Carol, Dave, Eva, Frank
    }

    @Test
    void findUserByEmail_shouldReturnMatchingUser() {
        Optional<User> user = userRepository.findUserByEmail("carol.teacher@example.com");
        assertThat(user).isNotNull();
        assertThat(user.get().getFirstName()).isEqualTo("Carol");
        assertThat(user.get().getLastName()).isEqualTo("Danvers");
    }

    @Test
    void findUserById_shouldReturnMatchingUser() {
        UUID aliceId = testDataPopulator.getUserAlice().getId();
        Optional<User> user = userRepository.findUserById(aliceId);
        assertThat(user).isNotNull();
        assertThat(user.get().getFirstName()).isEqualTo("Alice");
        assertThat(user.get().getLastName()).isEqualTo("Wonderland");
    }

    @Test
    void findUsersByEmailContaining_shouldReturnMatchingUsers() {
        List<User> users = userRepository.findUsersByEmailContaining("example.com");
        assertThat(users).hasSize(7);

        users = userRepository.findUsersByEmailContaining("alice.student");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    void searchByName_shouldReturnMatchingUsers() {
        List<User> users = userRepository.searchByName("Alice");
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getLastName()).isEqualTo("Wonderland");

        users = userRepository.searchByName("Wonderland"); // Changed from "Smith"
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getFirstName()).isEqualTo("Alice");

        users = userRepository.searchByName("li"); // Alice Wonderland, Carol Danvers
        assertThat(users).hasSize(1);
        assertThat(users).extracting(User::getFirstName).containsExactlyInAnyOrder("Alice");


        users = userRepository.searchByName("ev"); // Eva Peron
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getLastName()).isEqualTo("Peron");
    }

    @Test
    void findUsersByDepartmentName_shouldReturnMatchingUsers() {
        // Users in "Computer Science": Alice, Bob, Carol, Frank (from UserInDepartment)
        List<User> users = userRepository.findUsersByDepartmentName("Computer Science");
        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(4);
        assertThat(users).extracting(User::getFirstName).containsExactlyInAnyOrder("Alice", "Bob", "Carol", "Frank");
    }

    @Test
    void findUsersByDepartmentId_shouldReturnMatchingUsers() {
        UUID csDepartmentId = testDataPopulator.getCs().getId();
        // Users in CS Department: Alice, Bob, Carol, Frank
        List<User> users = userRepository.findUsersByDepartmentId(csDepartmentId);
        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(4);
        assertThat(users).extracting(User::getFirstName).containsExactlyInAnyOrder("Alice", "Bob", "Carol", "Frank");
    }

    @Test
    void findUsersWithAnyDepartmentAppointment_shouldReturnUsersWithAppointments() {
        // Appointments: app1 (Carlos, CS), app2 (Dave, Math), app3 (Eva, Physics), app4 (Frank, CS)
        List<User> users = userRepository.findUsersWithAnyDepartmentAppointment();
        assertThat(users).hasSize(4);
        assertThat(users).extracting(User::getFirstName).containsExactlyInAnyOrder("Carlos", "David", "Eva", "Frank");
    }

    @Test
    void findUsersWithDepartmentAppointmentInDateRange_shouldReturnMatchingUsers() {
        // app1: userCarlos, cs, Start: 2020-09-01T00:00:00Z, End: 2025-08-31T00:00:00Z
        // app2: userDave, math, Start: 2021-09-01T00:00:00Z, End: 2026-08-31T00:00:00Z
        // app3: userEva, physics, Start: 2022-09-01T00:00:00Z, End: null
        // app4: userFrank, cs, Start: 2023-09-01T00:00:00Z, End: null

        OffsetDateTime startDate1 = OffsetDateTime.of(2023, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endDate1 = OffsetDateTime.of(2023, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC);
        List<User> users1 = userRepository.findUsersWithDepartmentAppointmentInDateRange(startDate1, endDate1);
        // Carlos (app1), David (app2), Eva (app3), Frank (app4) all overlap or are within this broader range interpretation
        // Let's re-check logic: da.startDate <= endDate AND (da.endDate IS NULL OR da.endDate >= startDate)
        // app1 (Carlos): 2020-09-01 <= 2023-12-31 (T) AND 2025-08-31 >= 2023-06-01 (T) -> Carlos
        // app2 (Dave): 2021-09-01 <= 2023-12-31 (T) AND 2026-08-31 >= 2023-06-01 (T) -> David
        // app3 (Eva): 2022-09-01 <= 2023-12-31 (T) AND (null OR ...) (T) -> Eva
        // app4 (Frank): 2023-09-01 <= 2023-12-31 (T) AND (null OR ...) (T) -> Frank
        assertThat(users1).extracting(User::getFirstName).containsExactlyInAnyOrder("Carlos", "David", "Eva", "Frank");

        OffsetDateTime startDate2 = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endDate2 = OffsetDateTime.of(2022, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC);
        List<User> users2 = userRepository.findUsersWithDepartmentAppointmentInDateRange(startDate2, endDate2);
        // app1 (Carlos): 2020-09-01 <= 2022-12-31 (T) AND 2025-08-31 >= 2022-01-01 (T) -> Carlos
        // app2 (Dave): 2021-09-01 <= 2022-12-31 (T) AND 2026-08-31 >= 2022-01-01 (T) -> David
        // app3 (Eva): 2022-09-01 <= 2022-12-31 (T) AND (null OR ...) (T) -> Eva
        assertThat(users2).extracting(User::getFirstName).containsExactlyInAnyOrder("Carlos", "David", "Eva");

        OffsetDateTime startDate3 = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endDate3 = OffsetDateTime.of(2025, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC); // Current date is 14 June 2025
        List<User> users3 = userRepository.findUsersWithDepartmentAppointmentInDateRange(startDate3, endDate3);
        // app1 (Carlos): 2020-09-01 <= 2025-12-31 (T) AND 2025-08-31 >= 2024-01-01 (T) -> Carlos
        // app2 (Dave): 2021-09-01 <= 2025-12-31 (T) AND 2026-08-31 >= 2024-01-01 (T) -> David
        // app3 (Eva): 2022-09-01 <= 2025-12-31 (T) AND (null OR ...) (T) -> Eva
        // app4 (Frank): 2023-09-01 <= 2025-12-31 (T) AND (null OR ...) (T) -> Frank
        assertThat(users3).extracting(User::getFirstName).containsExactlyInAnyOrder("Carlos", "David", "Eva", "Frank");
    }

    @Test
    void findUsersWithDepartmentAppointmentForDepartmentName_shouldReturnMatchingUsers() {
        List<User> usersCS = userRepository.findUsersWithDepartmentAppointmentForDepartmentName("Computer Science");
        // app1 (Carlos, CS), app4 (Frank, CS)
        assertThat(usersCS).hasSize(2);
        assertThat(usersCS).extracting(User::getFirstName).containsExactlyInAnyOrder("Carlos", "Frank");

        List<User> usersMath = userRepository.findUsersWithDepartmentAppointmentForDepartmentName("Mathematics");
        // app2 (Dave, Math)
        assertThat(usersMath).hasSize(1);
        assertThat(usersMath.get(0).getFirstName()).isEqualTo("David");
    }

    @Test
    void findUsersWithDepartmentAppointmentForDepartmentId_shouldReturnMatchingUsers() {
        UUID csDepartmentId = testDataPopulator.getCs().getId();
        List<User> usersCS = userRepository.findUsersWithDepartmentAppointmentForDepartmentId(csDepartmentId);
        assertThat(usersCS).hasSize(2);
        assertThat(usersCS).extracting(User::getFirstName).containsExactlyInAnyOrder("Carlos", "Frank");

        UUID mathDepartmentId = testDataPopulator.getMath().getId();
        List<User> usersMath = userRepository.findUsersWithDepartmentAppointmentForDepartmentId(mathDepartmentId);
        assertThat(usersMath).hasSize(1);
        assertThat(usersMath.get(0).getFirstName()).isEqualTo("David");
    }

    @Test
    void findCurrentUsersWithDepartmentAppointmentForDepartmentName_shouldReturnMatchingUsers() {
        OffsetDateTime currentDate = OffsetDateTime.now(ZoneOffset.UTC); // Using OffsetDateTime.now(ZoneOffset.UTC) for consistency

        List<User> usersMath = userRepository.findCurrentUsersWithDepartmentAppointmentForDepartmentName("Mathematics", currentDate);
        // app2 (Dave, Math): 2021-09-01 to 2026-08-31. Is current.
        assertThat(usersMath).hasSize(1);
        assertThat(usersMath.get(0).getFirstName()).isEqualTo("David");

        List<User> usersCS = userRepository.findCurrentUsersWithDepartmentAppointmentForDepartmentName("Computer Science", currentDate);
        // app1 (Carlos, CS): 2020-09-01 to 2025-08-31. Is current.
        // app4 (Frank, CS): 2023-09-01 to null. Is current.
        assertThat(usersCS).hasSize(2);
        assertThat(usersCS).extracting(User::getFirstName).containsExactlyInAnyOrder("Carlos", "Frank");
    }

    @Test
    void findCurrentUsersWithDepartmentAppointmentForDepartmentId_shouldReturnMatchingUsers() {
        OffsetDateTime currentDate = OffsetDateTime.now(ZoneOffset.UTC); // Using OffsetDateTime.now(ZoneOffset.UTC)
        UUID mathDepartmentId = testDataPopulator.getMath().getId();
        List<User> usersMath = userRepository.findCurrentUsersWithDepartmentAppointmentForDepartmentId(mathDepartmentId, currentDate);
        assertThat(usersMath).hasSize(1);
        assertThat(usersMath.get(0).getFirstName()).isEqualTo("David");

        UUID csDepartmentId = testDataPopulator.getCs().getId();
        List<User> usersCS = userRepository.findCurrentUsersWithDepartmentAppointmentForDepartmentId(csDepartmentId, currentDate);
        assertThat(usersCS).hasSize(2);
        assertThat(usersCS).extracting(User::getFirstName).containsExactlyInAnyOrder("Carlos", "Frank");
    }
}
