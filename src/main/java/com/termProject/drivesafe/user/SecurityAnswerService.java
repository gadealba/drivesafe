package com.termProject.drivesafe.user;

import com.termProject.drivesafe.exceptions.InvalidSecurityAnswersException;
import com.termProject.drivesafe.securityquestions.SecurityQuestionModel;
import com.termProject.drivesafe.securityquestions.SecurityQuestionRepo;
import com.termProject.drivesafe.securityquestions.dto.SecurityAnswerRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SecurityAnswerService {

    /** How many security questions each new user must answer. Change this to change the rule. */
    public static final int REQUIRED_ANSWER_COUNT = 3;

    /** Longest answer accepted (BCrypt only handles about 72 bytes of input). */
    private static final int MAX_ANSWER_LENGTH = 64;

    private final SecurityQuestionRepo questionRepo;
    private final UserSecurityAnswerRepo answerRepo;
    private final PasswordEncoder encoder;

    public SecurityAnswerService(SecurityQuestionRepo questionRepo,
                                 UserSecurityAnswerRepo answerRepo,
                                 PasswordEncoder encoder) {
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
        this.encoder = encoder;
    }

    /**
     * Checks the submitted answers and builds the rows to save (hashed, no user attached yet).
     * Call this BEFORE saving the user, so a bad request never creates a user.
     */
    public List<UserSecurityAnswer> prepare(List<SecurityAnswerRequest> requests) {
        if (requests == null || requests.size() != REQUIRED_ANSWER_COUNT) {
            throw new InvalidSecurityAnswersException(
                "Please answer exactly " + REQUIRED_ANSWER_COUNT + " security questions.");
        }

        Set<Long> questionIds = new HashSet<>();
        for (SecurityAnswerRequest r : requests) {
            if (r == null || r.questionId() == null || r.answer() == null || r.answer().isBlank()) {
                throw new InvalidSecurityAnswersException("Every security question needs an answer.");
            }
            if (r.answer().trim().length() > MAX_ANSWER_LENGTH) {
                throw new InvalidSecurityAnswersException(
                    "Security answers must be " + MAX_ANSWER_LENGTH + " characters or fewer.");
            }
            if (!questionIds.add(r.questionId())) {
                throw new InvalidSecurityAnswersException("Each security question can only be chosen once.");
            }
        }

        Map<Long, SecurityQuestionModel> questions = questionRepo.findAllById(questionIds).stream()
            .collect(Collectors.toMap(SecurityQuestionModel::getId, q -> q));
        if (questions.size() != questionIds.size()) {
            throw new InvalidSecurityAnswersException("One or more security questions do not exist.");
        }

        return requests.stream()
            .map(r -> new UserSecurityAnswer(
                questions.get(r.questionId()),
                encoder.encode(normalize(r.answer()))))
            .toList();
    }

    /** Attaches the prepared answers to a saved user and stores them. */
    public void save(UserModel user, List<UserSecurityAnswer> prepared) {
        prepared.forEach(a -> a.setUser(user));
        answerRepo.saveAll(prepared);
    }

    /**
     * Cleans an answer the same way when it is saved and when it is checked later,
     * so "  Fido " and "fido" count as the same answer.
     */
    public static String normalize(String answer) {
        return answer.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
