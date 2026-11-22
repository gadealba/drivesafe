package com.termProject.drivesafe.user.dto;
import java.util.List;

import com.termProject.drivesafe.securityquestions.dto.SecurityAnswerRequest;

public record ClientUserRequest(String username, String password, String email,List<SecurityAnswerRequest> securityAnswers) {}

