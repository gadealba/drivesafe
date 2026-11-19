package com.termProject.drivesafe.user;

import lombok.*;
import jakarta.persistence.*;

@Data
@Entity
public class User{
    private @Id @GeneratedValue long id;
    private String username;
    private String password;
    private String email;

    public User(){}

    void _init_(User aUser){
        aUser.setUsername("username");
        aUser.setPassword("password");
        aUser.setEmail("@gmail.com");

    }
}