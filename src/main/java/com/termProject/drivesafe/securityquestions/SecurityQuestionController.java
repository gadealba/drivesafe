package com.termProject.drivesafe.securityquestions;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.termProject.drivesafe.securityquestions.dto.SecurityQuestionResponse;

@RestController
@RequestMapping("/api/security-questions")
public class SecurityQuestionController {
    private final SecurityQuestionService service;

    public SecurityQuestionController(SecurityQuestionService service) {
        this.service = service;
    }

    /** The list of predetermined questions the client can offer at sign up. */
    @GetMapping
    List<SecurityQuestionResponse> all() {
        return service.getAll().stream().map(SecurityQuestionResponse::from).toList();
    }
}
