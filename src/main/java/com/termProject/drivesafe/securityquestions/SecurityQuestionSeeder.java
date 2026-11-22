package com.termProject.drivesafe.securityquestions;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Fills the security_questions table on startup, but only when it is empty.
 * The questions below are PLACEHOLDERS. Replace the text with your own.
 * To change questions after the first run, edit the rows in the database.
 */
@Component
public class SecurityQuestionSeeder implements CommandLineRunner {

    private static final List<String> QUESTIONS = List.of(
        "What was the name of your first pet?",
        "In what city were you born?",
        "What was the name of your first school?",
        "What was the make of your first car?",
        "What street did you grow up on?"
    );

    private final SecurityQuestionRepo repo;

    public SecurityQuestionSeeder(SecurityQuestionRepo repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        repo.saveAll(QUESTIONS.stream().map(SecurityQuestionModel::new).toList());
    }
}
