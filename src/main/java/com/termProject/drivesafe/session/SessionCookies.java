package com.termProject.drivesafe.session;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Builds the session cookie. HttpOnly keeps JavaScript from reading it; SameSite=Strict keeps other sites from using it. */
@Component
public class SessionCookies {

    public static final String NAME = "DRIVESAFE_SESSION";

    private final boolean secure;

    /** Set app.session.cookie-secure=true in application.properties once the site is served over HTTPS. */
    public SessionCookies(@Value("${app.session.cookie-secure:false}") boolean secure) {
        this.secure = secure;
    }

    public ResponseCookie issue(String token) {
        return base(token).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value)
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path("/");
    }
}
