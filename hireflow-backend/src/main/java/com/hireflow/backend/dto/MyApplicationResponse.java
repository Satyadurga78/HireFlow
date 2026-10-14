package com.hireflow.backend.dto;

import java.time.LocalDateTime;

public class MyApplicationResponse {

    private Long id;
    private Long jobId;
    private String title;
    private String companyName;
    private String location;
    private String jobType;
    private String description;
    private String status;
    private LocalDateTime appliedAt;

    public MyApplicationResponse(
            Long id,
            Long jobId,
            String title,
            String companyName,
            String location,
            String jobType,
            String description,
            String status,
            LocalDateTime appliedAt) {

        this.id = id;
        this.jobId = jobId;
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.jobType = jobType;
        this.description = description;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getTitle() {
        return title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getLocation() {
        return location;
    }

    public String getJobType() {
        return jobType;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }
}