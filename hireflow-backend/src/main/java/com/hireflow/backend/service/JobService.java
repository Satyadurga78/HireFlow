
package com.hireflow.backend.service;

import com.hireflow.backend.entity.Job;
import com.hireflow.backend.repository.JobRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(Job job, Long recruiterId) {
        job.setRecruiterId(recruiterId);
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByRecruiter(Long recruiterId) {
        return jobRepository.findByRecruiterId(recruiterId);
    }

    public Optional<Job> getJobByIdAndRecruiter(
            Long jobId, Long recruiterId) {

        return jobRepository.findById(jobId)
                .filter(job ->
                        recruiterId.equals(job.getRecruiterId()));
    }

    public Job updateJob(Long jobId, Long recruiterId, Job updatedJob) {

        Job existingJob = getJobByIdAndRecruiter(jobId, recruiterId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found or access denied"));

        existingJob.setTitle(updatedJob.getTitle());
        existingJob.setDescription(updatedJob.getDescription());
        existingJob.setRequiredSkills(updatedJob.getRequiredSkills());
        existingJob.setLocation(updatedJob.getLocation());
        existingJob.setSalary(updatedJob.getSalary());
        existingJob.setExperience(updatedJob.getExperience());
        existingJob.setEmploymentType(updatedJob.getEmploymentType());
        existingJob.setCompanyName(updatedJob.getCompanyName());

        return jobRepository.save(existingJob);
    }

    public void deleteJob(Long jobId, Long recruiterId) {

        Job existingJob = getJobByIdAndRecruiter(jobId, recruiterId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found or access denied"));

        jobRepository.delete(existingJob);
    }
}