package com.breakingjob.authservice.dto.response;

import com.breakingjob.authservice.type.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private Long id;
    private String jwtToken;
    private RoleType role;
}