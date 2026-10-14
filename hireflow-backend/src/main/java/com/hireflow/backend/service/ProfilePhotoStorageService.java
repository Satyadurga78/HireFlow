
package com.hireflow.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ProfilePhotoStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final Path uploadDirectory =
            Paths.get(
                    System.getProperty("user.home"),
                    "HireFlowUploads",
                    "profile-photos"
            ).toAbsolutePath().normalize();

    public String storePhoto(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a photo");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Photo size must be less than 5 MB"
            );
        }

        byte[] bytes = file.getBytes();

        BufferedImage image;

        try {
            image = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid image file");
        }

        if (image == null) {
            throw new IllegalArgumentException(
                    "Only valid JPG and PNG images are allowed"
            );
        }

        String format = detectImageFormat(bytes);

        if (!"jpg".equals(format) && !"png".equals(format)) {
            throw new IllegalArgumentException(
                    "Only JPG and PNG images are allowed"
            );
        }

        Files.createDirectories(uploadDirectory);

        String filename = UUID.randomUUID() + "." + format;

        Path destination = uploadDirectory.resolve(filename)
                .normalize();

        if (!destination.getParent().equals(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        Files.write(destination, bytes);

        return filename;
    }

    public byte[] readPhoto(String filename) throws IOException {

        Path filePath = resolvePhotoPath(filename);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new java.nio.file.NoSuchFileException(filename);
        }

        return Files.readAllBytes(filePath);
    }

    public void deletePhoto(String filename) throws IOException {

        if (filename == null || filename.isBlank()) {
            return;
        }

        Path filePath = resolvePhotoPath(filename);

        Files.deleteIfExists(filePath);
    }

    private Path resolvePhotoPath(String filename) {

        if (filename == null
                || !filename.matches(
                        "[a-fA-F0-9-]+\\.(jpg|png)"
                )) {
            throw new IllegalArgumentException("Invalid photo filename");
        }

        Path filePath = uploadDirectory.resolve(filename)
                .normalize();

        if (!filePath.getParent().equals(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid photo path");
        }

        return filePath;
    }

    private String detectImageFormat(byte[] bytes) {

        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47) {
            return "png";
        }

        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "jpg";
        }

        return "";
    }
}