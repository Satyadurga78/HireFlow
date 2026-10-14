
package com.hireflow.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class ResumeController {

    private final Path uploadDir =
            Paths.get("uploads", "resumes")
                    .toAbsolutePath()
                    .normalize();

    @PostMapping(
        value = "/resume",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a PDF file"));
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null ||
                !originalFilename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only PDF files are allowed"));
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "PDF must be smaller than 5 MB"));
        }

        Files.createDirectories(uploadDir);

        String storedFilename =
                UUID.randomUUID().toString() + ".pdf";

        Path destination =
                uploadDir.resolve(storedFilename).normalize();

        if (!destination.startsWith(uploadDir)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid file path"));
        }

        Files.copy(
                file.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of(
                        "message", "Resume uploaded successfully",
                        "originalFilename", originalFilename,
                        "storedFilename", storedFilename
                ));
    }
}