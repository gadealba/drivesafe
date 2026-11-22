package com.termProject.drivesafe.user;
import com.termProject.drivesafe.exceptions.*;
import com.termProject.drivesafe.securityquestions.dto.SecurityAnswerRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;



@Service
public class UserService {
    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final SecurityAnswerService answerService;

    public UserService(UserRepo repo, PasswordEncoder encoder, SecurityAnswerService answerService) {
        this.repo = repo;
        this.encoder = encoder;
        this.answerService = answerService;
    }

    public List<UserModel> getAll() {
        return repo.findAll();
    }

    public UserModel getById(long id) {
        return repo.findById(id)
            .orElseThrow(() -> new UserNotFoundException("No user with id " + id));
    }

    @Transactional // all three operations must succeed or fail together
    public UserModel create(String username, String rawPassword, String email,List<SecurityAnswerRequest> securityAnswers) {
        if (repo.existsByUsername(username)) {
            throw new DuplicateUserException("Username already taken");
        }
        List<UserSecurityAnswer> answers = answerService.prepare(securityAnswers);
        UserModel user = repo.save(new UserModel(username, encoder.encode(rawPassword), email));
        answerService.save(user, answers);
        return user;
    }

    @Transactional
    public UserModel updateUsername(String email, String newUsername) {
        UserModel user = repo.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No user with email " + email));
        if (repo.existsByUsername(newUsername)) {
            throw new DuplicateUserException("Username already taken");
        }
        user.setUsername(newUsername);
        return repo.save(user);
    }
    
    public UserModel authenticate(String username, String rawPassword) {
    if (username == null || rawPassword == null) {
        throw new InvalidCredentialsException();
    }
    UserModel user = repo.findByUsername(username.trim())
        .orElseThrow(InvalidCredentialsException::new);

    if (!encoder.matches(rawPassword, user.getPassword())) {
        throw new InvalidCredentialsException();
    }
    return user;
}

}