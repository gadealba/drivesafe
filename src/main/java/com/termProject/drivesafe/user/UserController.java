package com.termProject.drivesafe.user;

import java.util.*;
import org.springframework.web.bind.annotation.*;

import com.termProject.drivesafe.user.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

 
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    


    public UserController(UserService service) {
        this.service = service;
    }
    @GetMapping("/getAll")
    List<ClientUserResponse> all() {
        return service.getAll().stream().map(ClientUserResponse::from).toList();
    }

    @PostMapping("/create")
    ResponseEntity<ClientUserResponse> create(@RequestBody ClientUserRequest request) {
        UserModel created = service.create(
    request.username(), request.password(), request.email(), request.securityAnswers());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClientUserResponse.from(created));
    }
    @PutMapping("/update/username")
    ClientUserResponse updateUsername(@RequestBody UpdateUsernameRequest request) {
        return ClientUserResponse.from(
            service.updateUsername(request.email(), request.newUsername()));
    }
    @PostMapping("/login")
    ResponseEntity<ClientUserResponse> login(@RequestBody ClientLoginRequest request) {
        UserModel user = service.authenticate(request.username(), request.password());
        return ResponseEntity.ok(ClientUserResponse.from(user));
}
    
}
