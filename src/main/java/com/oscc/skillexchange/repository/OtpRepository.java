package com.oscc.skillexchange.repository;

import com.oscc.skillexchange.domain.entity.OtpRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpRepository extends MongoRepository<OtpRecord, String> {

    Optional<OtpRecord> findByEmailIgnoreCase(String email);

    void deleteByEmailIgnoreCase(String email);

    void deleteByExpiresAtBefore(Instant instant);
}
