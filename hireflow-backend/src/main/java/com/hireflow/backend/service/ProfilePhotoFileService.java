
package com.hireflow.backend.service;

import com.hireflow.backend.entity.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProfilePhotoFileService {

    private final Path uploadDirectory;

    public ProfilePhotoFileService(
            @Value("${hireflow.upload-dir:uploads}") String uploadDir) {

        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize()
                .resolve("profile-photos")
                .normalize();
    }

    public byte[] getPhotoBytes(User user) {

        String savedPhotoPath = user.getProfilePhotoPath();

        if (savedPhotoPath == null || savedPhotoPath.isBlank()) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "This candidate has not uploaded a profile photo"
            );
        }

        /*
         * IMPORTANT:
         * profilePhotoPath must store only the generated filename,
         * for example: 2d45a1e3-abc4-4d82-9a11-photo.jpg
         * It must not store a user-supplied path.
         */
        Path photoPath = uploadDirectory
                .resolve(savedPhotoPath)
                .normalize();

        if (!photoPath.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Profile photo not found"
            );
        }

        try {
            if (!Files.isRegularFile(photoPath)) {
                throw new ResponseStatusException(
                        NOT_FOUND,
                        "Profile photo file not found"
                );
            }

            return Files.readAllBytes(photoPath);

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Unable to read profile photo"
            );
        }
    }

    public MediaType getPhotoMediaType(User user) {

        String filename = user.getProfilePhotoPath();

        if (filename == null) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Profile photo not found"
            );
        }

        String lower = filename.toLowerCase();

        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }

        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }

        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }

        throw new ResponseStatusException(
                NOT_FOUND,
                "Unsupported profile photo format"
        );
    }
}