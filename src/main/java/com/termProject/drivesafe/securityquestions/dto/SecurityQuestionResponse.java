package com.termProject.drivesafe.securityquestions.dto;

import com.termProject.drivesafe.securityquestions.SecurityQuestionModel;

/** What the client sees for each question: the id to send back, and the text to display. */
public record SecurityQuestionResponse(Long id, String text) {
    public static SecurityQuestionResponse from(SecurityQuestionModel q) {
        return new SecurityQuestionResponse(q.getId(), q.getText());
    }
}
