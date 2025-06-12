package com.uni.ethesis.data.repo;

import com.uni.ethesis.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> , JpaSpecificationExecutor<User> {
    List<User> findUsersByFirstNameAndLastName(String firstName, String lastName);
    List<User> findUsersByFirstName(String firstName);
    List<User> findUsersByLastName(String lastName);
    User findUserByEmail(String email);
    List<User> findUsersByEmailContaining(String partialEmail);
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<User> searchByName(@Param("text") String text);

}