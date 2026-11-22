package com.termProject.drivesafe.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class SessionManagerTest {

    @Test
    void getInstanceAlwaysReturnsTheSameObject() {
        assertSame(SessionManager.getInstance(), SessionManager.getInstance());
    }

    @Test
    void constructorIsPrivateSoNoSecondInstanceCanBeCreated() {
        var constructors = SessionManager.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPrivate(constructors[0].getModifiers()));
    }

    @Test
    void aCreatedSessionValidatesAndReturnsItsUser() {
        SessionManager manager = SessionManager.getInstance();
        String token = manager.createSession(7L, "gabe");

        var session = manager.validate(token);

        assertTrue(session.isPresent());
        assertEquals(7L, session.get().userId());
        assertEquals("gabe", session.get().username());
    }

    @Test
    void everySessionGetsADifferentToken() {
        SessionManager manager = SessionManager.getInstance();
        assertNotEquals(manager.createSession(1L, "a"), manager.createSession(1L, "a"));
    }

    @Test
    void unknownOrMissingTokensAreRejected() {
        SessionManager manager = SessionManager.getInstance();
        assertTrue(manager.validate("not-a-real-token").isEmpty());
        assertTrue(manager.validate("").isEmpty());
        assertTrue(manager.validate(null).isEmpty());
    }

    @Test
    void anInvalidatedSessionStopsWorking() {
        SessionManager manager = SessionManager.getInstance();
        String token = manager.createSession(3L, "sam");

        manager.invalidate(token);

        assertTrue(manager.validate(token).isEmpty());
    }
}
