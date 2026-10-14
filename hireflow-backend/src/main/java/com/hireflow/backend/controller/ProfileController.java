
package com.hireflow.backend.controller;

import com.hireflow.backend.dto.ProfileResponse;
import com.hireflow.backend.dto.ProfileUpdateRequest;
import com.hireflow.backend.service.ProfileService;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // Get logged-in user's profile
    @GetMapping
    public ProfileResponse getProfile(
            Authentication authentication) {

        return profileService.getProfile(
                authentication.getName()
        );
    }

    // Update logged-in user's profile
    @PutMapping
    public ProfileResponse updateProfile(
            Authentication authentication,
            @RequestBody ProfileUpdateRequest request) {

        return profileService.updateProfile(
                authentication.getName(),
                request
        );
    }

    // ================= PROFILE PHOTO =================

    @PostMapping(
            value = "/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ProfileResponse uploadProfilePhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        return profileService.uploadProfilePhoto(
                authentication.getName(),
                file
        );
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> getProfilePhoto(
            Authentication authentication) throws IOException {

        byte[] photo = profileService.getProfilePhoto(
                authentication.getName()
        );

        return ResponseEntity.ok()
                .contentType(detectMediaType(photo))
                .cacheControl(CacheControl.noStore())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"profile-photo\""
                )
                .body(photo);
    }

    @DeleteMapping("/photo")
    public ResponseEntity<Void> deleteProfilePhoto(
            Authentication authentication) throws IOException {

        profileService.deleteProfilePhoto(
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    // ================= RESUME =================

    // Candidate uploads or replaces resume
    @PostMapping(
            value = "/resume",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ProfileResponse uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        return profileService.uploadResume(
                authentication.getName(),
                file
        );
    }

    // Candidate downloads own resume
    @GetMapping("/resume")
    public ResponseEntity<byte[]> downloadOwnResume(
            Authentication authentication) throws IOException {

        byte[] resume = profileService.getOwnResume(
                authentication.getName()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .cacheControl(CacheControl.noStore())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"HireFlow-Resume.pdf\""
                )
                .body(resume);
    }

    // Candidate deletes own resume
    @DeleteMapping("/resume")
    public ResponseEntity<Void> deleteOwnResume(
            Authentication authentication) throws IOException {

        profileService.deleteOwnResume(
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    // ================= HELPERS =================

    private MediaType detectMediaType(byte[] bytes) {

        if (bytes != null
                && bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47) {

            return MediaType.IMAGE_PNG;
        }

        if (bytes != null
                && bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {

            return MediaType.IMAGE_JPEG;
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }
}