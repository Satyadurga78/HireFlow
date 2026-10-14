package com.hireflow.backend.controller;

import com.hireflow.backend.dto.ApplicantResponse;
import com.hireflow.backend.dto.MyApplicationResponse;
import com.hireflow.backend.entity.Application;
import com.hireflow.backend.entity.User;
import com.hireflow.backend.repository.UserRepository;
import com.hireflow.backend.service.ApplicationService;
import com.hireflow.backend.service.ProfilePhotoStorageService;
import com.hireflow.backend.service.ResumeStorageService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;
    private final ProfilePhotoStorageService profilePhotoStorageService;
    private final ResumeStorageService resumeStorageService;

    public ApplicationController(
            ApplicationService applicationService,
            UserRepository userRepository,
            ProfilePhotoStorageService profilePhotoStorageService,
            ResumeStorageService resumeStorageService) {

        this.applicationService = applicationService;
        this.userRepository = userRepository;
        this.profilePhotoStorageService = profilePhotoStorageService;
        this.resumeStorageService = resumeStorageService;
    }

    private User getLoggedInUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // Candidate applies for a job
    @PostMapping("/{jobId}")
    public Application applyForJob(
            @PathVariable Long jobId,
            Authentication authentication) {

        User candidate = getLoggedInUser(authentication);

        if (!"CANDIDATE".equalsIgnoreCase(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can apply for jobs"
            );
        }

        try {
            return applicationService.applyForJob(
                    candidate.getId(),
                    jobId
            );
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }

    // Candidate views own applications
    @GetMapping("/my-applications")
    public List<MyApplicationResponse> getMyApplications(
            Authentication authentication) {

        User candidate = getLoggedInUser(authentication);

        if (!"CANDIDATE".equalsIgnoreCase(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can view their applications"
            );
        }

        return applicationService.getMyApplications(
                candidate.getId()
        );
    }

    // Candidate withdraws application
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<?> deleteMyApplication(
            @PathVariable Long applicationId,
            Authentication authentication) {

        User candidate = getLoggedInUser(authentication);

        if (!"CANDIDATE".equalsIgnoreCase(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can withdraw applications"
            );
        }

        try {
            applicationService.deleteMyApplication(
                    applicationId,
                    candidate.getId()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Application withdrawn successfully"
                    )
            );

        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }

    // Recruiter views applicants for one job
    @GetMapping("/job/{jobId}")
    public List<ApplicantResponse> getApplicantsForJob(
            @PathVariable Long jobId,
            Authentication authentication) {

        User recruiter = getLoggedInUser(authentication);

        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can view applicants"
            );
        }

        try {
            return applicationService.getApplicantsForJob(
                    jobId,
                    recruiter.getId()
            );

        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }

    // Recruiter views all applicants across own jobs
    @GetMapping("/all")
    public List<ApplicantResponse> getAllApplicantsForRecruiter(
            Authentication authentication) {

        User recruiter = getLoggedInUser(authentication);

        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can view applicants"
            );
        }

        return applicationService.getAllApplicantsForRecruiter(
                recruiter.getId()
        );
    }

    // Recruiter views candidate photo for an application
    @GetMapping("/{applicationId}/candidate-photo")
    public ResponseEntity<byte[]> getCandidatePhoto(
            @PathVariable Long applicationId,
            Authentication authentication) {

        User recruiter = getLoggedInUser(authentication);

        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can view applicant photos"
            );
        }

        try {
            User candidate =
                    applicationService.getCandidateForApplicationPhoto(
                            applicationId,
                            recruiter.getId()
                    );

            String photoPath = candidate.getProfilePhotoPath();

            if (photoPath == null || photoPath.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Candidate has not uploaded a profile photo"
                );
            }

            byte[] photoBytes;

            try {
                photoBytes = profilePhotoStorageService.readPhoto(
                        photoPath
                );

            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to read candidate photo",
                        exception
                );
            }

            MediaType mediaType = getPhotoMediaType(photoPath);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CACHE_CONTROL,
                            "private, no-store, no-cache, must-revalidate"
                    )
                    .contentType(mediaType)
                    .body(photoBytes);

        } catch (AccessDeniedException exception) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    exception.getMessage()
            );
        }
    }

    // Recruiter downloads candidate resume
    @GetMapping("/{applicationId}/resume")
    public ResponseEntity<byte[]> downloadCandidateResume(
            @PathVariable Long applicationId,
            Authentication authentication) {

        User recruiter = getLoggedInUser(authentication);

        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can download applicant resumes"
            );
        }

        try {
            User candidate =
                    applicationService.getCandidateForApplicationPhoto(
                            applicationId,
                            recruiter.getId()
                    );

            String resumePath = candidate.getResumePath();

            if (resumePath == null || resumePath.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Candidate has not uploaded a resume"
                );
            }

            byte[] resumeBytes;

            try {
                resumeBytes = resumeStorageService.readResume(
                        resumePath
                );

            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to read candidate resume",
                        exception
                );
            }

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CACHE_CONTROL,
                            "private, no-store, no-cache, must-revalidate"
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"candidate-resume.pdf\""
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resumeBytes.length)
                    .body(resumeBytes);

        } catch (AccessDeniedException exception) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    exception.getMessage()
            );
        }
    }

    // Recruiter gets SkillMatch analysis for an applicant
    @GetMapping("/{applicationId}/skillmatch")
    public ResponseEntity<?> getApplicantSkillMatch(
            @PathVariable Long applicationId,
            Authentication authentication) {

        User recruiter = getLoggedInUser(authentication);

        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can analyze applicant resumes"
            );
        }

        try {
            Map<String, Object> result =
                    applicationService.getSkillMatchForApplication(
                            applicationId,
                            recruiter.getId()
                    );

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CACHE_CONTROL,
                            "private, no-store"
                    )
                    .body(result);

        } catch (AccessDeniedException exception) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    exception.getMessage()
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );

        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }

    // Determine image type from stored path
    private MediaType getPhotoMediaType(String photoPath) {

        String path = photoPath.toLowerCase();

        if (path.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }

        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }

        throw new ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported profile photo format"
        );
    }

    // Recruiter shortlists or rejects an applicant
    @PutMapping("/{applicationId}/status")
    public Application updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        User recruiter = getLoggedInUser(authentication);

        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can update application status"
            );
        }

        String status = request.get("status");

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status is required"
            );
        }

        try {
            return applicationService.updateApplicationStatus(
                    applicationId,
                    recruiter.getId(),
                    status
            );

        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }
}