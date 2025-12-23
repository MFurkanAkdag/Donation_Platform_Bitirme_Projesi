package com.seffafbagis.api.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for file storage operations.
 */
@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:/api/v1/files}")
    private String baseUrl;

    public String storeFile(MultipartFile file, String subFolder) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(uploadDir, subFolder);

        if (!Files.exists(targetLocation)) {
            Files.createDirectories(targetLocation);
        }

        Path filePath = targetLocation.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return baseUrl + "/" + subFolder + "/" + fileName;
    }

    public void deleteFile(String fileUrl) throws IOException {
        String relativePath = fileUrl.replace(baseUrl + "/", "");
        Path filePath = Paths.get(uploadDir, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    public byte[] loadFile(String fileName, String subFolder) throws IOException {
        Path filePath = Paths.get(uploadDir, subFolder, fileName);
        return Files.readAllBytes(filePath);
    }
}
