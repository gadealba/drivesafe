package com.termProject.drivesafe.user;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSecurityAnswerRepo extends JpaRepository<UserSecurityAnswer, Long> {
    List<UserSecurityAnswer> findByUserId(Long userId);
}
