package com.skillexchange.repository;

import com.skillexchange.model.OtpDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpDetails, java.util.UUID> {
    Optional<OtpDetails> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}

