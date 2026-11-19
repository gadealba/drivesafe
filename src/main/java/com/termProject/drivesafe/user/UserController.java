package com.termProject.drivesafe.user;

import org.springframework.web.bind.annotation.*;



 
@RestController("")
public class UserController {
    private User aUser = new User();

    @GetMapping("/getUser")
    User getUser() {
        aUser._init_(aUser);
        return aUser;
    }

}