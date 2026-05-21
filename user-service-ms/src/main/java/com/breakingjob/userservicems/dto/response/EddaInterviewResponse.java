package com.breakingjob.userservicems.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EddaInterviewResponse {

    @JsonProperty("meeting_url")
    private String meetingUrl;

}