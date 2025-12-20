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

    @Query("{ 'skills': ?0, 'email': { $ne: ?1 } }")
    List<User> findBySkillsContainingAndEmailNot(String skill, String email);

    @Query("{ 'email': { $ne: ?0 } }")
    List<User> findAllExcludingEmail(String email);

    @Query(value = "{ }", fields = "{ 'skills': 1 }")
    List<User> findAllProjectedSkills();
}

