package com.oscc.skillexchange.service;

import com.oscc.skillexchange.domain.entity.User;
import com.oscc.skillexchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MatchEngine for finding users with matching skills and interests
 * Matches users where NewUser.skillsOffered intersects with ExistingUser.interests
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchEngine {

    private final UserRepository userRepository;

    /**
     * Calculate similarity between two lists (Jaccard similarity)
     */
    private double calculateSimilarity(List<String> list1, List<String> list2) {
        if (list1 == null || list1.isEmpty() || list2 == null || list2.isEmpty()) {
            return 0.0;
        }

        Set<String> set1 = new HashSet<>(list1);
        Set<String> set2 = new HashSet<>(list2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * Find potential matches when a user's skillsOffered match other users' interests
     * Returns matches with similarity > 80%
     */
    public List<MatchResult> findMatches(User newUser) {
        log.info("Finding matches for user: {}", newUser.getEmail());

        List<String> skillsOffered = newUser.getSkillsOffered() != null 
                ? newUser.getSkillsOffered() 
                : new ArrayList<>();

        if (skillsOffered.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> allUsers = userRepository.findAllExcludingEmail(newUser.getEmail());

        List<MatchResult> matches = allUsers.stream()
                .filter(user -> user.getInterests() != null && !user.getInterests().isEmpty())
                .map(user -> {
                    double similarity = calculateSimilarity(skillsOffered, user.getInterests());
                    return new MatchResult(user, similarity, findMatchingSkills(skillsOffered, user.getInterests()));
                })
                .filter(match -> match.getSimilarity() > 0.80) // 80% threshold
                .sorted(Comparator.comparing(MatchResult::getSimilarity).reversed())
                .collect(Collectors.toList());

        log.info("Found {} matches for user: {}", matches.size(), newUser.getEmail());
        return matches;
    }

    /**
     * Find matching skills between skillsOffered and interests
     */
    private List<String> findMatchingSkills(List<String> skillsOffered, List<String> interests) {
        Set<String> skillsSet = new HashSet<>(skillsOffered);
        Set<String> interestsSet = new HashSet<>(interests);
        
        skillsSet.retainAll(interestsSet);
        return new ArrayList<>(skillsSet);
    }

    /**
     * Match result containing user, similarity score, and matching skills
     */
    public static class MatchResult {
        private final User user;
        private final double similarity;
        private final List<String> matchingSkills;

        public MatchResult(User user, double similarity, List<String> matchingSkills) {
            this.user = user;
            this.similarity = similarity;
            this.matchingSkills = matchingSkills;
        }

        public User getUser() {
            return user;
        }

        public double getSimilarity() {
            return similarity;
        }

        public List<String> getMatchingSkills() {
            return matchingSkills;
        }
    }
}
