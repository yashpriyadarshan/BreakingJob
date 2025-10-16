package com.breakingjob.jobms.job;

import com.breakingjob.jobms.job.external.Company;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobWithCompanyDTO {
    private Job job;
    private Company company;
}
