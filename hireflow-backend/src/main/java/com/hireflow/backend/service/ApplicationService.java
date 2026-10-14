package com.hireflow.backend.service;

import com.hireflow.backend.dto.ApplicantResponse;
import com.hireflow.backend.dto.MyApplicationResponse;
import com.hireflow.backend.entity.Application;
import com.hireflow.backend.entity.Job;
import com.hireflow.backend.entity.User;
import com.hireflow.backend.repository.ApplicationRepository;
import com.hireflow.backend.repository.InterviewRepository;
import com.hireflow.backend.repository.JobRepository;
import com.hireflow.backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillMatchService skillMatchService;
    private final InterviewRepository interviewRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            SkillMatchService skillMatchService,
            InterviewRepository interviewRepository) {

        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.skillMatchService = skillMatchService;
        this.interviewRepository = interviewRepository;
    }

    // Applicant row for the recruiter, including the candidate's profile details
    private ApplicantResponse toResponse(Application application, User candidate, Job job) {

        ApplicantResponse response = new ApplicantResponse(
                application.getId(),
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                application.getStatus(),
                application.getAppliedAt(),
                job.getId(),
                job.getTitle()
        );

        response.setCandidatePhone(candidate.getPhone());
        response.setCandidateLocation(candidate.getLocation());
        response.setCandidateEducation(candidate.getEducation());
        response.setCandidateExperience(candidate.getExperience());
        response.setCandidateSkills(candidate.getSkills());
        response.setCandidateBio(candidate.getBio());

        return response;
    }

    // Candidate applies for a job
    public Application applyForJob(
            Long candidateId,
            Long jobId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        if (applicationRepository
                .findByCandidateIdAndJobId(candidateId, jobId)
                .isPresent()) {

            throw new RuntimeException(
                    "You have already applied for this job");
        }

        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setJobId(job.getId());
        application.setStatus("APPLIED");

        return applicationRepository.save(application);
    }

    // Candidate views their applications
    public List<MyApplicationResponse> getMyApplications(
            Long candidateId) {

        List<Application> applications =
                applicationRepository.findByCandidateId(candidateId);

        return applications.stream()
                .map(application -> {

                    Job job = jobRepository
                            .findById(application.getJobId())
                            .orElse(null);

                    if (job == null) {
                        return new MyApplicationResponse(
                                application.getId(),
                                application.getJobId(),
                                "Job no longer available",
                                "Company",
                                "Location not specified",
                                "Not specified",
                                "This job is no longer available.",
                                application.getStatus(),
                                application.getAppliedAt()
                        );
                    }

                    return new MyApplicationResponse(
                            application.getId(),
                            job.getId(),
                            job.getTitle(),
                            job.getCompanyName(),
                            job.getLocation(),
                            job.getEmploymentType(),
                            job.getDescription(),
                            application.getStatus(),
                            application.getAppliedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    // Candidate withdraws their application
    @Transactional
    public void deleteMyApplication(
            Long applicationId,
            Long candidateId) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Application not found"));

        if (!candidateId.equals(application.getCandidateId())) {
            throw new RuntimeException(
                    "You cannot delete another candidate's application");
        }

        interviewRepository.deleteByApplicationId(applicationId);
        applicationRepository.delete(application);
    }

    // Applicants for one specific job
    public List<ApplicantResponse> getApplicantsForJob(
            Long jobId,
            Long recruiterId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        if (!recruiterId.equals(job.getRecruiterId())) {
            throw new RuntimeException(
                    "You cannot view applicants for another recruiter's job");
        }

        List<Application> applications =
                applicationRepository.findByJobId(jobId);

        return applications.stream()
                .map(application -> {

                    User candidate = userRepository
                            .findById(application.getCandidateId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Candidate not found"));

                    return toResponse(application, candidate, job);
                })
                .collect(Collectors.toList());
    }

    // All applicants across this recruiter's jobs
    public List<ApplicantResponse> getAllApplicantsForRecruiter(
            Long recruiterId) {

        List<Job> recruiterJobs = jobRepository.findAll()
                .stream()
                .filter(job ->
                        recruiterId.equals(job.getRecruiterId()))
                .collect(Collectors.toList());

        return recruiterJobs.stream()
                .flatMap(job ->
                        applicationRepository.findByJobId(job.getId())
                                .stream()
                                .map(application -> {

                                    User candidate = userRepository
                                            .findById(
                                                    application.getCandidateId())
                                            .orElseThrow(() ->
                                                    new RuntimeException(
                                                            "Candidate not found"));

                                    return toResponse(application, candidate, job);
                                })
                )
                .collect(Collectors.toList());
    }

    // Recruiter updates application status
    public Application updateApplicationStatus(
            Long applicationId,
            Long recruiterId,
            String status) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Application not found"));

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        if (!recruiterId.equals(job.getRecruiterId())) {
            throw new RuntimeException(
                    "You cannot update applications for another recruiter's job");
        }

        if (!List.of("SHORTLISTED", "INTERVIEW", "SELECTED", "REJECTED").contains(status)) {

            throw new RuntimeException(
                    "Invalid application status");
        }

        application.setStatus(status);

        return applicationRepository.save(application);
    }

    // Securely get candidate for recruiter photo and resume retrieval
    @Transactional(readOnly = true)
    public User getCandidateForApplicationPhoto(
            Long applicationId,
            Long recruiterId) {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        Job job = jobRepository
                .findById(application.getJobId())
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        if (!recruiterId.equals(job.getRecruiterId())) {
            throw new AccessDeniedException(
                    "You are not authorized to view this candidate"
            );
        }

        return userRepository
                .findById(application.getCandidateId())
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));
    }

    // Recruiter securely gets SkillMatch analysis for an applicant
    @Transactional(readOnly = true)
    public Map<String, Object> getSkillMatchForApplication(
            Long applicationId,
            Long recruiterId) throws IOException {

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));

        Job job = jobRepository
                .findById(application.getJobId())
                .orElseThrow(() ->
                        new RuntimeException("Job not found"));

        // Verify recruiter owns the job
        if (!recruiterId.equals(job.getRecruiterId())) {
            throw new AccessDeniedException(
                    "You are not authorized to analyze this applicant"
            );
        }

        User candidate = userRepository
                .findById(application.getCandidateId())
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        // Analyze the resume uploaded by this candidate
        // against the job linked to this application
        return skillMatchService.matchApplicantResumeWithJob(
                candidate,
                job
        );
    }
}