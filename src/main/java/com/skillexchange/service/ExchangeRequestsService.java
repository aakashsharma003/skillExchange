package com.skillexchange.service;

import com.skillexchange.model.request.ExchangeRequest;
import com.skillexchange.repository.ExchangeRequestRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExchangeRequestsService {

    private final ExchangeRequestRepository exchangeRequestRepository;

    public ExchangeRequestsService(ExchangeRequestRepository exchangeRequestRepository) {
        this.exchangeRequestRepository = exchangeRequestRepository;
    }

    // create / update
    public ExchangeRequest saveExchangeRequest(ExchangeRequest request) {
        return exchangeRequestRepository.save(request);
    }

    // reads
    public List<ExchangeRequest> findAllReceiver(UUID receiverId) {
        return exchangeRequestRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    public List<ExchangeRequest> findAllSender(UUID senderId) {
        return exchangeRequestRepository.findBySenderIdOrderByCreatedAtDesc(senderId);
    }

    public Optional<ExchangeRequest> findById(UUID requestId) {
        return exchangeRequestRepository.findById(requestId);
    }

    // Backward-compatible helper if callers still pass String IDs
    public Optional<ExchangeRequest> findById(String requestId) {
        try {
            return exchangeRequestRepository.findById(UUID.fromString(requestId));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Optional<ExchangeRequest> findBySenderReceiverAndSkill(UUID senderId,
                                                                 UUID receiverId,
                                                                 String skill) {
        // Accept both "pending"/"Pending" + "Accepted"
        return exchangeRequestRepository.findFirstBySenderIdAndReceiverIdAndRequestedSkillAndStatusIn(
            senderId, receiverId, skill, List.of("pending", "Pending", "Accepted")
        );
    }
}
