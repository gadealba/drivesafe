package com.termProject.drivesafe.securityquestions.dto;

/** One chosen question and the user's answer, as sent by the client. */
public record SecurityAnswerRequest(Long questionId, String answer) {}
