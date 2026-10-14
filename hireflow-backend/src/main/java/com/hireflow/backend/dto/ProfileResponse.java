
package com.hireflow.backend.dto;

public class ProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String location;
    private String education;
    private String skills;
    private boolean resumeUploaded;
    private boolean profilePhotoUploaded;
    private String experience;
    private String bio;
    private String companyName;
    private String companyLocation;
    private String companyDescription;

    public ProfileResponse() {
    }

    public ProfileResponse(
            Long id,
            String name,
            String email,
            String phone,
            String location,
            String education,
            String skills,
            boolean resumeUploaded,
            boolean profilePhotoUploaded) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.education = education;
        this.skills = skills;
        this.resumeUploaded = resumeUploaded;
        this.profilePhotoUploaded = profilePhotoUploaded;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getEducation() {
        return education;
    }

    public String getSkills() {
        return skills;
    }

    public boolean isResumeUploaded() {
        return resumeUploaded;
    }

    public boolean isProfilePhotoUploaded() {
        return profilePhotoUploaded;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyLocation() {
        return companyLocation;
    }

    public void setCompanyLocation(String companyLocation) {
        this.companyLocation = companyLocation;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }
}