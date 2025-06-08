package com.uni.ethesis.data.repo;

import com.uni.ethesis.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}