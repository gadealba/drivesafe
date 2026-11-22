package com.termProject.drivesafe.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Runs on every request. If the request carries a valid session cookie, the
 * user is marked as logged in for that request. Otherwise nothing is set and
 * Spring Security rejects the request when the route needs a login.
 */
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionManager sessions;

    public SessionAuthenticationFilter(SessionManager sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        sessions.validate(readToken(request)).ifPresent(session ->
            SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                    session, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))));

        chain.doFilter(request, response);
    }

    private static String readToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (SessionCookies.NAME.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
