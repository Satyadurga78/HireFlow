package com.hireflow.backend.controller;

import com.hireflow.backend.entity.Interview;
import com.hireflow.backend.entity.User;
import com.hireflow.backend.repository.UserRepository;
import com.hireflow.backend.service.InterviewService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;
    private final UserRepository userRepository;

    public InterviewController(
            InterviewService interviewService,
            UserRepository userRepository) {

        this.interviewService = interviewService;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser(Authentication authentication) {

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // Recruiter schedules or reschedules an interview.
    // Body: { applicationId, interviewDate (ISO e.g. 2026-10-12T10:30), interviewType }
    @PostMapping
    public Interview schedule(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        User recruiter = getLoggedInUser(authentication);

        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can schedule interviews"
            );
        }

        Object rawId = request.get("applicationId");

        if (rawId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Application is required"
            );
        }

        try {
            return interviewService.schedule(
                    recruiter.getId(),
                    Long.valueOf(rawId.toString()),
                    request.get("interviewDate") == null
                            ? null : request.get("interviewDate").toString(),
                    request.get("interviewType") == null
                            ? null : request.get("interviewType").toString()
            );

        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage()
            );
        }
    }

    // Candidate: own interviews. Recruiter: interviews for own jobs.
    @GetMapping
    public List<Interview> getMyInterviews(Authentication authentication) {

        User user = getLoggedInUser(authentication);

        if ("RECRUITER".equalsIgnoreCase(user.getRole())) {
            return interviewService.getForRecruiter(user.getId());
        }

        return interviewService.getForCandidate(user.getId());
    }
}
