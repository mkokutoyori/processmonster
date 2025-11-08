package com.processmonster.bpm.service;

import com.processmonster.bpm.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service for file storage operations
 */
@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;
    private final MessageSource messageSource;
    private final long maxFileSize;
    private final List<String> allowedMimeTypes;

    public FileStorageService(
            @Value("${app.upload.dir:./uploads}") String uploadDir,
            @Value("${app.upload.max-size:10485760}") long maxFileSize, // 10MB default
            @Value("${app.upload.allowed-types:image/jpeg,image/png,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document}") String allowedTypes,
            MessageSource messageSource) {

        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        this.allowedMimeTypes = Arrays.asList(allowedTypes.split(","));
        this.messageSource = messageSource;

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage initialized at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new BusinessException("Could not create upload directory", ex);
        }
    }

    /**
     * Store file with validation
     */
    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Generate unique file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // Check for path traversal attack
            if (originalFileName.contains("..")) {
                throw new BusinessException(
                        getMessage("file.invalid-path", originalFileName));
            }

            // Copy file to target location
            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {} -> {}", originalFileName, storedFileName);
            return storedFileName;

        } catch (IOException ex) {
            throw new BusinessException(
                    getMessage("file.store-failed", originalFileName), ex);
        }
    }

    /**
     * Store file with custom path
     */
    public String storeFile(MultipartFile file, String subDirectory) {
        Path subDir = this.fileStorageLocation.resolve(subDirectory).normalize();
        try {
            Files.createDirectories(subDir);
        } catch (IOException ex) {
            throw new BusinessException("Could not create subdirectory: " + subDirectory, ex);
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = subDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return subDirectory + "/" + storedFileName;

        } catch (IOException ex) {
            throw new BusinessException(
                    getMessage("file.store-failed", originalFileName), ex);
        }
    }

    /**
     * Load file as Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new BusinessException(getMessage("file.not-found", fileName));
            }
        } catch (MalformedURLException ex) {
            throw new BusinessException(getMessage("file.not-found", fileName), ex);
        }
    }

    /**
     * Delete file
     */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileName);
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", fileName, ex);
            throw new BusinessException(getMessage("file.delete-failed", fileName), ex);
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.size(filePath);
        } catch (IOException ex) {
            throw new BusinessException(getMessage("file.not-found", fileName), ex);
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new BusinessException(getMessage("file.empty"));
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(
                    getMessage("file.too-large",
                            formatFileSize(file.getSize()),
                            formatFileSize(maxFileSize)));
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType)) {
            throw new BusinessException(
                    getMessage("file.invalid-type", contentType));
        }

        // Check file name
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.contains("..")) {
            throw new BusinessException(getMessage("file.invalid-name", fileName));
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) return "";

        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    /**
     * Format file size in human-readable format
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Get localized message
     */
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }
}
