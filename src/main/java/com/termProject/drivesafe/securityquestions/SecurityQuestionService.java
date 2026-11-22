package com.termProject.drivesafe.securityquestions;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SecurityQuestionService {
    private final SecurityQuestionRepo repo;

    public SecurityQuestionService(SecurityQuestionRepo repo) {
        this.repo = repo;
    }

    public List<SecurityQuestionModel> getAll() {
        return repo.findAll();
    }
}
