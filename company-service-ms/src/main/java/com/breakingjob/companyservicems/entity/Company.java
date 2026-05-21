package com.breakingjob.companyservicems.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    private Long id; // SAME as Auth Service userId

    private String name;

    private String address;

    @Column(length = 1000)
    private String description;

    private String website;

    private String location;

    private String logoUrl;

    private String phone;

    private String email;
}