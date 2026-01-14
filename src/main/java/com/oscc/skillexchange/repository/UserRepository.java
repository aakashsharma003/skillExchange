package com.oscc.skillexchange.repository;

import com.oscc.skillexchange.domain.entity.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // Search by skillsOffered
    @Query("{ 'skillsOffered': { $in: [?0] }, 'email': { $ne: ?1 } }")
    List<User> findBySkillsOfferedContainingAndEmailNot(String skill, String email);

    // Search by interests
    @Query("{ 'interests': { $in: [?0] }, 'email': { $ne: ?1 } }")
    List<User> findByInterestsContainingAndEmailNot(String interest, String email);

    @Query("{ 'email': { $ne: ?0 } }")
    List<User> findAllExcludingEmail(String email);

    @Query(value = "{ }", fields = "{ 'skillsOffered': 1, 'interests': 1 }")
    List<User> findAllProjectedSkills();
}

