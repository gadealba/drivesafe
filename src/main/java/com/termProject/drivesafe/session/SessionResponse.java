package com.termProject.drivesafe.session;

/** Who is logged in. Never contains the token. */
public record SessionResponse(long userId, String username) {}
