package com.breakingjob.userservicems.repository;

import com.breakingjob.userservicems.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
