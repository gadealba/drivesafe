package com.termProject.drivesafe.user;

import com.termProject.drivesafe.securityquestions.SecurityQuestionModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One user's answer to one security question. Many rows per user, one row per question.
 * The answer is stored only as a hash, exactly like a password.
 */
@Entity
@Table(
    name = "user_security_answers",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class UserSecurityAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private SecurityQuestionModel question;

    /** BCrypt hash of the normalized answer. The plain answer is never stored. */
    @Column(nullable = false)
    private String answerHash;

    public UserSecurityAnswer(SecurityQuestionModel question, String answerHash) {
        this.question = question;
        this.answerHash = answerHash;
    }
}
