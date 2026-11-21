package com.termProject.drivesafe.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepo extends JpaRepository<UserModel, Long>{
    Optional<UserModel> findByEmail(String email);
    boolean existsByUsername(String username);
}
