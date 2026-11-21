package com.termProject.drivesafe.user;

import java.util.*;
import org.springframework.web.bind.annotation.*;



 
@RestController
public class UserController {
    private User aUser = new User();
    private List<User> userList= new ArrayList<>();

    @GetMapping("/getUser")
    User getUser() {
        return aUser;
    }
    @PostMapping("/addUser")
    User addUser(@RequestBody User newUser) {
        userList.add(newUser);
        return newUser;
    }
    @GetMapping("/getUserById/{id}")
    User getUserById(@PathVariable long id) {
        for (User user : userList) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null; // or throw an exception if user not found
    }
}