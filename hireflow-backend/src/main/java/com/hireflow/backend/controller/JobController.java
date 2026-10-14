
package com.hireflow.backend.controller;

import com.hireflow.backend.entity.Job;
import com.hireflow.backend.entity.User;
import com.hireflow.backend.repository.UserRepository;
import com.hireflow.backend.service.JobService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;

    public JobController(
            JobService jobService,
            UserRepository userRepository) {

        this.jobService = jobService;
        this.userRepository = userRepository;
    }

    private User getLoggedInRecruiter(
            Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please login first"
            );
        }

        String email = authentication.getName();

        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recruiter not found"
                ));

        if (!"RECRUITER".equals(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can perform this action"
            );
        }

        return recruiter;
    }

    // Create a job
    @PostMapping
    public Job createJob(
            @RequestBody Job job,
            Authentication authentication) {

        User recruiter = getLoggedInRecruiter(authentication);

        return jobService.createJob(
                job,
                recruiter.getId()
        );
    }

    // View all jobs posted by all recruiters
    @GetMapping
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    // View jobs created by the logged-in recruiter
    @GetMapping("/my-jobs")
    public List<Job> getMyJobs(
            Authentication authentication) {

        User recruiter = getLoggedInRecruiter(authentication);

        return jobService.getJobsByRecruiter(
                recruiter.getId()
        );
    }

    // Update only the logged-in recruiter's own job
    @PutMapping("/{id}")
    public Job updateJob(
            @PathVariable Long id,
            @RequestBody Job updatedJob,
            Authentication authentication) {

        User recruiter = getLoggedInRecruiter(authentication);

        try {
            return jobService.updateJob(
                    id,
                    recruiter.getId(),
                    updatedJob
            );
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Job not found or access denied"
            );
        }
    }

    // Delete only the logged-in recruiter's own job
    @DeleteMapping("/{id}")
    public String deleteJob(
            @PathVariable Long id,
            Authentication authentication) {

        User recruiter = getLoggedInRecruiter(authentication);

        try {
            jobService.deleteJob(
                    id,
                    recruiter.getId()
            );

            return "Job deleted successfully";

        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Job not found or access denied"
            );
        }
    }
}