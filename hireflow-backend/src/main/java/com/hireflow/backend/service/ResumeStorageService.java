
package com.hireflow.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ResumeStorageService {

    private static final long MAX_FILE_SIZE =
            5 * 1024 * 1024;

    private final Path storageDirectory;

    public ResumeStorageService() throws IOException {
        this.storageDirectory = Paths.get(
                System.getProperty("user.home"),
                "hireflow-storage",
                "resumes"
        ).toAbsolutePath().normalize();

        Files.createDirectories(storageDirectory);
    }

    // Validate and store PDF
    public String storeResume(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a resume PDF"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Resume must be smaller than 5 MB"
            );
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null
                || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "Only PDF files are allowed"
            );
        }

        byte[] bytes = file.getBytes();

        if (!isPdf(bytes)) {
            throw new IllegalArgumentException(
                    "Invalid PDF file"
            );
        }

        String fileName =
                UUID.randomUUID().toString() + ".pdf";

        Path destination = storageDirectory
                .resolve(fileName)
                .normalize();

        if (!destination.startsWith(storageDirectory)) {
            throw new IOException(
                    "Invalid resume file path"
            );
        }

        Files.write(destination, bytes);

        return fileName;
    }

    // Read stored PDF
    public byte[] readResume(String storedFileName)
            throws IOException {

        Path filePath = resolveSafePath(storedFileName);

        if (!Files.exists(filePath)
                || !Files.isRegularFile(filePath)) {
            throw new java.nio.file.NoSuchFileException(
                    "Resume not found"
            );
        }

        return Files.readAllBytes(filePath);
    }

    // Delete stored PDF
    public void deleteResume(String storedFileName)
            throws IOException {

        Path filePath = resolveSafePath(storedFileName);

        Files.deleteIfExists(filePath);
    }

    // Ensure stored name cannot escape storage directory
    private Path resolveSafePath(String storedFileName)
            throws IOException {

        if (storedFileName == null
                || !storedFileName.matches(
                        "[a-fA-F0-9-]+\\.pdf")) {

            throw new IOException(
                    "Invalid resume path"
            );
        }

        Path filePath = storageDirectory
                .resolve(storedFileName)
                .normalize();

        if (!filePath.startsWith(storageDirectory)) {
            throw new IOException(
                    "Invalid resume path"
            );
        }

        return filePath;
    }

    // Check PDF file signature
    private boolean isPdf(byte[] bytes) {

        if (bytes == null || bytes.length < 5) {
            return false;
        }

        return bytes[0] == '%'
                && bytes[1] == 'P'
                && bytes[2] == 'D'
                && bytes[3] == 'F'
                && bytes[4] == '-';
    }
}