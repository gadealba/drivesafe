package com.termProject.drivesafe.session;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionManager sessions;
    private final SessionCookies cookies;

    public SessionController(SessionManager sessions, SessionCookies cookies) {
        this.sessions = sessions;
        this.cookies = cookies;
    }

    /** "Am I logged in?" Returns the user while the session is valid. Security answers 401 otherwise. */
    @GetMapping
    SessionResponse current(@AuthenticationPrincipal SessionManager.UserSession session) {
        return new SessionResponse(session.userId(), session.username());
    }

    /** Ends the session on the server and clears the cookie. Safe to call when already logged out. */
    @PostMapping("/logout")
    ResponseEntity<Void> logout(@CookieValue(name = SessionCookies.NAME, required = false) String token) {
        sessions.invalidate(token);
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
            .build();
    }
}
