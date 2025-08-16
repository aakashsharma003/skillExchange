package com.skillexchange.repository;

import com.skillexchange.model.request.ExchangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, UUID> {

    List<ExchangeRequest> findByReceiverIdOrderByCreatedAtDesc(UUID receiverId);

    List<ExchangeRequest> findBySenderIdOrderByCreatedAtDesc(UUID senderId);

    Optional<ExchangeRequest> findFirstBySenderIdAndReceiverIdAndRequestedSkillAndStatusIn(
        UUID senderId, UUID receiverId, String requestedSkill, List<String> statuses
    );
}
