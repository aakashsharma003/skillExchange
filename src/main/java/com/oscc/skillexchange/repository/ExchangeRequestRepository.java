package com.oscc.skillexchange.repository;

import com.oscc.skillexchange.domain.entity.ExchangeRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRequestRepository extends MongoRepository<ExchangeRequest, String> {

    List<ExchangeRequest> findByReceiverIdOrderByCreatedAtDesc(String receiverId);

    List<ExchangeRequest> findBySenderIdOrderByCreatedAtDesc(String senderId);

    @Query("{ 'senderId': ?0, 'receiverId': ?1, 'requestedSkill': ?2, 'status': { $in: ?3 } }")
    Optional<ExchangeRequest> findBySenderAndReceiverAndSkillAndStatusIn(
            String senderId,
            String receiverId,
            String requestedSkill,
            List<ExchangeRequest.RequestStatus> statuses
    );

    @Query("{ $or: [ { 'senderId': ?0 }, { 'receiverId': ?0 } ], 'status': { $in: ?1 } }")
    List<ExchangeRequest> findByUserIdAndStatusIn(String userId, List<ExchangeRequest.RequestStatus> statuses);
}