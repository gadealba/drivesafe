package com.termProject.drivesafe.user.dto;

import com.termProject.drivesafe.user.UserModel;
public record ClientUserResponse(Long id, String username, String email) {
    public static ClientUserResponse from(UserModel u) {
        return new ClientUserResponse(u.getId(), u.getUsername(), u.getEmail());
    }
}