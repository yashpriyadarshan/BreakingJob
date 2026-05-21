package com.breakingjob.authservice.entity;

import com.breakingjob.authservice.type.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String username;

    private String firstName;
    private String lastName;

    private String email;
    private String phone;

    private String password;

    private RoleType role; // CANDIDATE, RECRUITER
}
