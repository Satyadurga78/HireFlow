package com.hireflow.backend.service;

import com.hireflow.backend.entity.Application;
import com.hireflow.backend.entity.Interview;
import com.hireflow.backend.entity.Job;
import com.hireflow.backend.repository.ApplicationRepository;
import com.hireflow.backend.repository.InterviewRepository;
import com.hireflow.backend.repository.JobRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterviewService {

    private static final Set<String> TYPES = Set.of("Online", "In-person", "Phone");

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public InterviewService(
            InterviewRepository interviewRepository,
            ApplicationRepository applicationRepository,
            JobRepository jobRepository) {

        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    // Recruiter schedules (or reschedules) the interview for an application.
    // The application moves to the INTERVIEW stage in the same transaction.
    @Transactional
    public Interview schedule(
            Long recruiterId,
            Long applicationId,
            String interviewDate,
            String interviewType) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        if (!recruiterId.equals(job.getRecruiterId())) {
            throw new RuntimeException(
                    "You cannot schedule interviews for another recruiter's job");
        }

        if ("REJECTED".equals(application.getStatus())) {
            throw new RuntimeException(
                    "Cannot schedule an interview for a rejected application");
        }

        if (interviewDate == null || interviewDate.isBlank()) {
            throw new RuntimeException("Interview date is required");
        }

        LocalDateTime when;
        try {
            when = LocalDateTime.parse(interviewDate);
        } catch (DateTimeParseException exception) {
            throw new RuntimeException("Invalid interview date");
        }

        if (when.isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Interview date must be in the future");
        }

        if (interviewType == null || !TYPES.contains(interviewType)) {
            throw new RuntimeException(
                    "Interview type must be Online, In-person or Phone");
        }

        Interview interview = interviewRepository
                .findByApplicationId(applicationId)
                .orElseGet(Interview::new);

        interview.setApplicationId(applicationId);
        interview.setInterviewDate(when);
        interview.setInterviewType(interviewType);
        interview.setStatus("SCHEDULED");

        application.setStatus("INTERVIEW");
        applicationRepository.save(application);

        return interviewRepository.save(interview);
    }

    // Candidate: interviews for their own applications
    @Transactional(readOnly = true)
    public List<Interview> getForCandidate(Long candidateId) {

        List<Long> applicationIds = applicationRepository
                .findByCandidateId(candidateId)
                .stream()
                .map(Application::getId)
                .collect(Collectors.toList());

        return findByApplications(applicationIds);
    }

    // Recruiter: interviews for applications to their own jobs
    @Transactional(readOnly = true)
    public List<Interview> getForRecruiter(Long recruiterId) {

        List<Long> applicationIds = jobRepository
                .findByRecruiterId(recruiterId)
                .stream()
                .flatMap(job -> applicationRepository
                        .findByJobId(job.getId())
                        .stream())
                .map(Application::getId)
                .collect(Collectors.toList());

        return findByApplications(applicationIds);
    }

    private List<Interview> findByApplications(List<Long> applicationIds) {

        if (applicationIds.isEmpty()) {
            return List.of();
        }

        return interviewRepository.findByApplicationIdIn(applicationIds);
    }
}
