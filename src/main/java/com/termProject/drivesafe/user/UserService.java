package com.termProject.drivesafe.user;
import com.termProject.drivesafe.exceptions.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;


@Service
public class UserService {
    private final UserRepo repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepo repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public List<UserModel> getAll() {
        return repo.findAll();
    }

    public UserModel getById(long id) {
        return repo.findById(id)
            .orElseThrow(() -> new UserNotFoundException("No user with id " + id));
    }

    public UserModel create(String username, String rawPassword, String email) {
        if (repo.existsByUsername(username)) {
            throw new DuplicateUserException("Username already taken");
        }
        return repo.save(new UserModel(username, encoder.encode(rawPassword), email));
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
}