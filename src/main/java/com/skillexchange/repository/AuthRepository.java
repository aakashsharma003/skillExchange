package com.skillexchange.repository;

import com.skillexchange.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthRepository extends JpaRepository<UserDetails, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<UserDetails> findByEmailIgnoreCase(String email);
}

