
package com.hireflow.backend.service;

import com.hireflow.backend.entity.Job;
import com.hireflow.backend.entity.User;
import com.hireflow.backend.repository.JobRepository;
import com.hireflow.backend.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class SkillMatchService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ResumeStorageService resumeStorageService;
    private final ResumeTextExtractionService textExtractionService;

    public SkillMatchService(
            UserRepository userRepository,
            JobRepository jobRepository,
            ResumeStorageService resumeStorageService,
            ResumeTextExtractionService textExtractionService) {

        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.resumeStorageService = resumeStorageService;
        this.textExtractionService = textExtractionService;
    }

    private static final List<String> SKILL_LIST = Arrays.asList(
            "Java", "Python", "C", "C++", "C#",
            "JavaScript", "TypeScript",
            "HTML", "CSS", "React", "Angular",
            "Spring Boot", "Spring",
            "Node.js", "Express.js",
            "MySQL", "PostgreSQL", "MongoDB",
            "SQL", "Hibernate", "JPA",
            "REST APIs", "REST API",
            "Microservices",
            "Git", "GitHub", "Docker",
            "AWS", "Azure",
            "Data Structures", "Algorithms",
            "OOP", "Object-Oriented Programming",
            "DSA",
            "Machine Learning", "Deep Learning",
            "Artificial Intelligence",
            "TensorFlow", "PyTorch",
            "Pandas", "NumPy",
            "Communication", "Problem Solving",
            "Analytical Skills", "Teamwork",
            "Team Management", "Time Management",
            "Agile", "Linux", "Kubernetes",
            "Redis", "Postman", "Maven",
            "JUnit", "Jenkins", "Excel"
    );

    // Candidate analyzes own resume
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeResume(String email)
            throws IOException {

        User user = getCandidate(email);

        String resumeText = extractResume(user);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("candidateName", user.getName());
        response.put("resumeAnalyzed", true);
        response.put("extractedText", resumeText);

        return response;
    }

    // Candidate matches own resume with a job
    @Transactional(readOnly = true)
    public Map<String, Object> matchResumeWithJob(
            String email,
            Long jobId) throws IOException {

        User user = getCandidate(email);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new NoSuchElementException("Job not found"));

        return calculateMatch(user, job);
    }

    // Recruiter matches applicant's resume with the applied job
    @Transactional(readOnly = true)
    public Map<String, Object> matchApplicantResumeWithJob(
            User candidate,
            Job job) throws IOException {

        if (candidate == null || job == null) {
            throw new IllegalArgumentException(
                    "Candidate and job are required");
        }

        return calculateMatch(candidate, job);
    }

    // Common matching logic
    private Map<String, Object> calculateMatch(
            User candidate,
            Job job) throws IOException {

        String resumeText = extractResume(candidate);

        // Use the dedicated requiredSkills field first
        List<String> requiredSkills =
                getRequiredSkills(job);

        // Extract skills actually mentioned in resume
        List<String> candidateSkills =
                extractSkills(resumeText);

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String requiredSkill : requiredSkills) {

            boolean matched = false;

            for (String candidateSkill : candidateSkills) {

                if (isSameSkill(requiredSkill, candidateSkill)) {
                    matched = true;
                    break;
                }
            }

            if (matched) {
                matchedSkills.add(requiredSkill);
            } else {
                missingSkills.add(requiredSkill);
            }
        }

        double matchPercentage = 0;

        if (!requiredSkills.isEmpty()) {
            matchPercentage =
                    matchedSkills.size() * 100.0
                            / requiredSkills.size();
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("candidateName", candidate.getName());
        response.put("jobId", job.getId());
        response.put("jobTitle", job.getTitle());

        response.put("requiredSkills", requiredSkills);
        response.put("candidateSkills", candidateSkills);
        response.put("matchedSkills", matchedSkills);
        response.put("missingSkills", missingSkills);

        response.put(
                "matchPercentage",
                Math.round(matchPercentage * 100.0) / 100.0
        );

        if (requiredSkills.isEmpty()) {
            response.put(
                    "message",
                    "Please add required skills to this job"
            );
        }

        return response;
    }

    // Read recruiter-provided required skills
    private List<String> getRequiredSkills(Job job) {

        String requiredSkillsText = job.getRequiredSkills();

        // Fallback for older jobs that have no requiredSkills
        if (requiredSkillsText == null
                || requiredSkillsText.isBlank()) {

            requiredSkillsText =
                    job.getTitle() + " " + job.getDescription();

            return extractSkills(requiredSkillsText);
        }

        Set<String> uniqueSkills = new LinkedHashSet<>();

        // Required skills are stored as comma-separated values
        String[] skills = requiredSkillsText.split(",");

        for (String skill : skills) {

            String cleanedSkill = skill.trim();

            if (!cleanedSkill.isEmpty()) {
                uniqueSkills.add(cleanedSkill);
            }
        }

        return new ArrayList<>(uniqueSkills);
    }

    // Compare skill names and their common aliases
    private boolean isSameSkill(
            String requiredSkill,
            String candidateSkill) {

        Set<String> requiredAliases =
                getSkillAliases(requiredSkill);

        Set<String> candidateAliases =
                getSkillAliases(candidateSkill);

        for (String alias : requiredAliases) {

            if (candidateAliases.contains(alias)) {
                return true;
            }
        }

        return false;
    }

    // Normalize different names for the same skill
    private Set<String> getSkillAliases(String skill) {

        String normalized = skill
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9+#.]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        Set<String> aliases = new HashSet<>();
        aliases.add(normalized);

        switch (normalized) {

            case "dsa":
                aliases.add("data structures and algorithms");
                aliases.add("data structures");
                aliases.add("algorithms");
                break;

            case "data structures and algorithms":
                aliases.add("dsa");
                break;

            case "rest api":
            case "rest apis":
            case "restful api":
            case "restful apis":
                aliases.add("rest api");
                aliases.add("rest apis");
                aliases.add("restful api");
                aliases.add("restful apis");
                break;

            case "oop":
            case "oops":
            case "object oriented programming":
            case "object oriented programming concepts":
                aliases.add("oop");
                aliases.add("oops");
                aliases.add("object oriented programming");
                aliases.add("object oriented programming concepts");
                break;

            case "express":
            case "express js":
            case "express.js":
                aliases.add("express");
                aliases.add("express js");
                aliases.add("express.js");
                break;

            case "node":
            case "node js":
            case "node.js":
                aliases.add("node");
                aliases.add("node js");
                aliases.add("node.js");
                break;

            case "problem solving":
            case "problem solving skills":
                aliases.add("problem solving");
                aliases.add("problem solving skills");
                break;

            case "teamwork":
            case "team work":
            case "team collaboration":
                aliases.add("teamwork");
                aliases.add("team work");
                aliases.add("team collaboration");
                break;

            case "communication":
            case "communication skills":
                aliases.add("communication");
                aliases.add("communication skills");
                break;

            case "analytical":
            case "analytical skills":
            case "analytical thinking":
                aliases.add("analytical");
                aliases.add("analytical skills");
                aliases.add("analytical thinking");
                break;

            case "spring":
            case "spring framework":
                aliases.add("spring");
                aliases.add("spring framework");
                break;
        }

        return aliases;
    }

    // Only candidates can use candidate-specific endpoints
    private User getCandidate(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"CANDIDATE".equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException(
                    "Only candidates can use SkillMatch"
            );
        }

        return user;
    }

    // Extract text from uploaded resume
    private String extractResume(User user) throws IOException {

        String resumePath = user.getResumePath();

        if (resumePath == null || resumePath.isBlank()) {
            throw new NoSuchFileException(
                    "Please upload your resume first"
            );
        }

        byte[] resumeBytes =
                resumeStorageService.readResume(resumePath);

        return textExtractionService.extractText(resumeBytes);
    }

    // Detect recognized skills in resume or fallback job text
    private List<String> extractSkills(String text) {

        Set<String> foundSkills = new LinkedHashSet<>();

        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String normalizedText =
                text.toLowerCase(Locale.ROOT);

        for (String skill : SKILL_LIST) {

            String normalizedSkill =
                    skill.toLowerCase(Locale.ROOT);

            String regex = "(?<![a-z0-9])"
                    + Pattern.quote(normalizedSkill)
                    + "(?![a-z0-9])";

            if (Pattern.compile(regex)
                    .matcher(normalizedText)
                    .find()) {

                foundSkills.add(skill);
            }
        }

        // Recognize common resume abbreviations
        if (Pattern.compile(
                "(?<![a-z0-9])dsa(?![a-z0-9])")
                .matcher(normalizedText).find()) {

            foundSkills.add("DSA");
        }

        if (Pattern.compile(
                "(?<![a-z0-9])oops?(?![a-z0-9])")
                .matcher(normalizedText).find()) {

            foundSkills.add("OOP");
        }

        return new ArrayList<>(foundSkills);
    }
}