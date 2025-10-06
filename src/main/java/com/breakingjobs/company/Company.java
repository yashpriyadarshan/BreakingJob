package com.breakingjobs.company;

import com.breakingjobs.job.Job;
import com.breakingjobs.review.Review;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(nullable = false)
    private Long id;

    private String name;

    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "company") // mappedBy = "company" removes a separate table and makes a column in Job with company id
    private List<Job> jobs;

    @OneToMany(mappedBy = "company")
    private List<Review> reviews;
}
