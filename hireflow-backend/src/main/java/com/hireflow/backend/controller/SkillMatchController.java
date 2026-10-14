package com.hireflow.backend.controller;

import com.hireflow.backend.service.SkillMatchService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/skillmatch")
public class SkillMatchController {

    private final SkillMatchService skillMatchService;

    public SkillMatchController(
            SkillMatchService skillMatchService) {
        this.skillMatchService = skillMatchService;
    }

    @PostMapping("/analyze")
    public Map<String, Object> analyzeResume(
            Authentication authentication) throws IOException {

        return skillMatchService.analyzeResume(
                authentication.getName()
        );
    }

    @PostMapping("/match/{jobId}")
    public Map<String, Object> matchResumeWithJob(
            Authentication authentication,
            @PathVariable Long jobId) throws IOException {

        return skillMatchService.matchResumeWithJob(
                authentication.getName(),
                jobId
        );
    }
}