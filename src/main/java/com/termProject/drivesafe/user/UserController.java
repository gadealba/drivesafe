package com.termProject.drivesafe.user;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.termProject.drivesafe.user.DTO.*;

 
@RestController
@RequestMapping("/api/users")
public class UserController {
    // private UserModel aUser = new UserModel();
    private List<UserModel> userList= new ArrayList<>();
    private final UserRepo repo;
    private final PasswordEncoder encoder;
    

    UserController (UserRepo repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // @GetMapping("/getUser")
    // UserModel getUser() {
    //     return aUser;
    // }
    @GetMapping("/getAll")
    List<ClientUserResponse> all() {
        return repo.findAll().stream()
            .map(u -> new ClientUserResponse(u.getId(), u.getUsername(), u.getEmail()))
            .toList();
    }
    // @PostMapping("/addUser")
    // UserModel addUser(@RequestBody UserModel newUser) {
    //     userList.add(newUser);
    //     return newUser;
    // }
    @GetMapping("/getUserById/{id}")
    UserModel getUserById(@PathVariable long id) {
        for (UserModel user : userList) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null; // or throw an exception if user not found
    }
    @PostMapping("/create")
    ResponseEntity<ClientUserResponse> create(@RequestBody ClientUserRequest request) {
        UserModel newUser = repo.save(
            new UserModel(request.username(), encoder.encode(request.password()), request.email())
        );
        return ResponseEntity.status(HttpStatus.CREATED) // shows the client that the user was created successfully 
            .body(new ClientUserResponse(newUser.getId(), newUser.getUsername(), newUser.getEmail()));
    }
}
