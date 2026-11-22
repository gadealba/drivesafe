package com.termProject.drivesafe.securityquestions;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One predetermined question that users can choose to answer. Stored once, shared by everyone. */
@Data
@Entity
@Table(name = "security_questions")
@NoArgsConstructor
public class SecurityQuestionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String text;

    public SecurityQuestionModel(String text) {
        this.text = text;
    }
}
