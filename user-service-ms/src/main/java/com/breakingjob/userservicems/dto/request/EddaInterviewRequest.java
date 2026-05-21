package com.breakingjob.userservicems.dto.request;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EddaInterviewRequest {

    private Long userId;

    private String name;

    private List<String> skills;

    private List<String> projects;

    private List<String> experiences;

}