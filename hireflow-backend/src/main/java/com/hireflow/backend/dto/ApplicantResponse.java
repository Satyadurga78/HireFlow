package com.hireflow.backend.dto;

import java.time.LocalDateTime;

public class ApplicantResponse {

    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String status;
    private LocalDateTime appliedAt;

    private Long jobId;
    private String jobTitle;

    // Candidate details shown in the recruiter's profile view
    private String candidatePhone;
    private String candidateLocation;
    private String candidateEducation;
    private String candidateExperience;
    private String candidateSkills;
    private String candidateBio;

    public ApplicantResponse() {
    }

    public ApplicantResponse(
            Long applicationId,
            Long candidateId,
            String candidateName,
            String candidateEmail,
            String status,
            LocalDateTime appliedAt) {

        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public ApplicantResponse(
            Long applicationId,
            Long candidateId,
            String candidateName,
            String candidateEmail,
            String status,
            LocalDateTime appliedAt,
            Long jobId,
            String jobTitle) {

        this.applicationId = applicationId;
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.status = status;
        this.appliedAt = appliedAt;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCandidatePhone() {
        return candidatePhone;
    }

    public void setCandidatePhone(String candidatePhone) {
        this.candidatePhone = candidatePhone;
    }

    public String getCandidateLocation() {
        return candidateLocation;
    }

    public void setCandidateLocation(String candidateLocation) {
        this.candidateLocation = candidateLocation;
    }

    public String getCandidateEducation() {
        return candidateEducation;
    }

    public void setCandidateEducation(String candidateEducation) {
        this.candidateEducation = candidateEducation;
    }

    public String getCandidateExperience() {
        return candidateExperience;
    }

    public void setCandidateExperience(String candidateExperience) {
        this.candidateExperience = candidateExperience;
    }

    public String getCandidateSkills() {
        return candidateSkills;
    }

    public void setCandidateSkills(String candidateSkills) {
        this.candidateSkills = candidateSkills;
    }

    public String getCandidateBio() {
        return candidateBio;
    }

    public void setCandidateBio(String candidateBio) {
        this.candidateBio = candidateBio;
    }
}