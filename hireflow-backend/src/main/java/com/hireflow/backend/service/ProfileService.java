
package com.hireflow.backend.service;

import com.hireflow.backend.dto.ProfileResponse;
import com.hireflow.backend.dto.ProfileUpdateRequest;
import com.hireflow.backend.entity.User;
import com.hireflow.backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfilePhotoStorageService photoStorageService;
    private final ResumeStorageService resumeStorageService;

    public ProfileService(
            UserRepository userRepository,
            ProfilePhotoStorageService photoStorageService,
            ResumeStorageService resumeStorageService) {

        this.userRepository = userRepository;
        this.photoStorageService = photoStorageService;
        this.resumeStorageService = resumeStorageService;
    }

    // Get logged-in user's own profile
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {

        User user = getUser(email);

        return convertToResponse(user);
    }

    // Update logged-in user's own profile
    @Transactional
    public ProfileResponse updateProfile(
            String email,
            ProfileUpdateRequest request) {

        User user = getUser(email);

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setLocation(request.getLocation());
        user.setEducation(request.getEducation());
        user.setSkills(request.getSkills());

        // New fields: only overwrite when the client sent them
        if (request.getExperience() != null) user.setExperience(request.getExperience());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getCompanyName() != null) user.setCompanyName(request.getCompanyName());
        if (request.getCompanyLocation() != null) user.setCompanyLocation(request.getCompanyLocation());
        if (request.getCompanyDescription() != null) user.setCompanyDescription(request.getCompanyDescription());

        User savedUser = userRepository.save(user);

        return convertToResponse(savedUser);
    }

    // ================= PROFILE PHOTO =================

    @Transactional
    public ProfileResponse uploadProfilePhoto(
            String email,
            MultipartFile file) throws IOException {

        User user = getUser(email);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a profile photo"
            );
        }

        String oldPhoto = user.getProfilePhotoPath();

        String newPhoto =
                photoStorageService.storePhoto(file);

        try {
            user.setProfilePhotoPath(newPhoto);

            User savedUser = userRepository.save(user);

            if (oldPhoto != null && !oldPhoto.isBlank()) {
                try {
                    photoStorageService.deletePhoto(oldPhoto);
                } catch (IOException ignored) {
                    // Old file cleanup can be retried
                }
            }

            return convertToResponse(savedUser);

        } catch (RuntimeException exception) {

            try {
                photoStorageService.deletePhoto(newPhoto);
            } catch (IOException ignored) {
                // Preserve original error
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public byte[] getProfilePhoto(String email)
            throws IOException {

        User user = getUser(email);

        String photoPath = user.getProfilePhotoPath();

        if (photoPath == null || photoPath.isBlank()) {
            throw new NoSuchFileException(
                    "Profile photo not found"
            );
        }

        return photoStorageService.readPhoto(photoPath);
    }

    @Transactional
    public void deleteProfilePhoto(String email)
            throws IOException {

        User user = getUser(email);

        String photoPath = user.getProfilePhotoPath();

        if (photoPath == null || photoPath.isBlank()) {
            return;
        }

        user.setProfilePhotoPath(null);
        userRepository.save(user);

        try {
            photoStorageService.deletePhoto(photoPath);
        } catch (NoSuchFileException ignored) {
            // Database reference is already cleared
        }
    }

    // ================= RESUME =================

    // Upload or replace candidate resume
    @Transactional
    public ProfileResponse uploadResume(
            String email,
            MultipartFile file) throws IOException {

        User user = getUser(email);

        requireCandidate(user);

        String oldResume = user.getResumePath();

        // Store and validate new PDF
        String newResume =
                resumeStorageService.storeResume(file);

        try {
            user.setResumePath(newResume);

            User savedUser = userRepository.save(user);

            // Remove previous resume after database save
            if (oldResume != null && !oldResume.isBlank()) {
                try {
                    resumeStorageService.deleteResume(oldResume);
                } catch (IOException ignored) {
                    // Old file cleanup can be retried
                }
            }

            return convertToResponse(savedUser);

        } catch (RuntimeException exception) {

            try {
                resumeStorageService.deleteResume(newResume);
            } catch (IOException ignored) {
                // Preserve original database error
            }

            throw exception;
        }
    }

    // Candidate downloads own resume
    @Transactional(readOnly = true)
    public byte[] getOwnResume(String email)
            throws IOException {

        User user = getUser(email);

        requireCandidate(user);

        String resumePath = user.getResumePath();

        if (resumePath == null || resumePath.isBlank()) {
            throw new NoSuchFileException(
                    "No resume uploaded"
            );
        }

        return resumeStorageService.readResume(resumePath);
    }

    // Candidate deletes own resume
    @Transactional
    public void deleteOwnResume(String email)
            throws IOException {

        User user = getUser(email);

        requireCandidate(user);

        String resumePath = user.getResumePath();

        if (resumePath == null || resumePath.isBlank()) {
            return;
        }

        // Clear database reference first
        user.setResumePath(null);
        userRepository.save(user);

        try {
            resumeStorageService.deleteResume(resumePath);
        } catch (NoSuchFileException ignored) {
            // File is already missing
        }
    }

    // ================= HELPERS =================

    private void requireCandidate(User user) {

        if (!"CANDIDATE".equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException(
                    "Only candidates can manage resumes"
            );
        }
    }

    private User getUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String role = user.getRole();

        if (!"CANDIDATE".equalsIgnoreCase(role)
                && !"RECRUITER".equalsIgnoreCase(role)) {

            throw new AccessDeniedException(
                    "Only candidates and recruiters can access profiles"
            );
        }

        return user;
    }

    private ProfileResponse convertToResponse(User user) {

        ProfileResponse response = new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getLocation(),
                user.getEducation(),
                user.getSkills(),
                user.getResumePath() != null
                        && !user.getResumePath().isBlank(),
                user.getProfilePhotoPath() != null
                        && !user.getProfilePhotoPath().isBlank()
        );

        response.setExperience(user.getExperience());
        response.setBio(user.getBio());
        response.setCompanyName(user.getCompanyName());
        response.setCompanyLocation(user.getCompanyLocation());
        response.setCompanyDescription(user.getCompanyDescription());

        return response;
    }
}