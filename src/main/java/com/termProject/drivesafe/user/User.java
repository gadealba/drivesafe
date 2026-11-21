package com.termProject.drivesafe.user;

import lombok.*;
import jakarta.persistence.*;

@Data
@Entity
@Table (name = "users")
public class User{
    private @Id @GeneratedValue long id;
    private String username;
    private String password;
    private String email;

    public User(){}

    void setUser(User aUser){
       this.setUsername(aUser.getUsername());
        this.setPassword(aUser.getPassword());
        this.setEmail(aUser.getEmail());

    }
}