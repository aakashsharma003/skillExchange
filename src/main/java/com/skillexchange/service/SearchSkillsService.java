package com.skillexchange.service;

import java.util.List;
import com.skillexchange.model.UserDetails;
import com.skillexchange.repository.SearchSkillRepository;
import org.springframework.stereotype.Service;

@Service
public class SearchSkillsService {

    private final SearchSkillRepository searchSkillRepository;

    public SearchSkillsService(SearchSkillRepository searchSkillRepository) {
        this.searchSkillRepository = searchSkillRepository;
    }

    public List<UserDetails> findBySkillsContaining(String skill, String email) {
        return searchSkillRepository.findBySkillsContainingAndEmailNot(skill, email);
    }

    public List<UserDetails> getAllUsers(String email) {
        return searchSkillRepository.findAllExcludingEmail(email);
    }

    public List<String> getAllDistinctSkills() {
        return searchSkillRepository.findDistinctSkills();
    }
}
