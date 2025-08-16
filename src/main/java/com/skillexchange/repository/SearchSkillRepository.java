package com.skillexchange.repository;

import com.skillexchange.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SearchSkillRepository extends JpaRepository<UserDetails, UUID> {

    // Use a native query to check if the skills array contains the specific skill
    @Query(value = "SELECT * FROM skillexchange.users WHERE :skill = ANY(skills) AND LOWER(email) != LOWER(:email)", nativeQuery = true)
    List<UserDetails> findBySkillsContainingAndEmailNot(@Param("skill") String skill, @Param("email") String email);

    // All users except the given email (case-insensitive)
    @Query("SELECT u FROM UserDetails u WHERE LOWER(u.email) <> LOWER(:email)")
    List<UserDetails> findAllExcludingEmail(@Param("email") String email);

    // This is the correct way to get distinct skills from a PostgreSQL array
    @Query(value = "SELECT DISTINCT unnest(skills) FROM skillexchange.users", nativeQuery = true)
    List<String> findDistinctSkills();

}
