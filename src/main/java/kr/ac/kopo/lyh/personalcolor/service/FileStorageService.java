package kr.ac.kopo.lyh.personalcolor.service;

import kr.ac.kopo.lyh.personalcolor.config.ApplicationConfig;
import kr.ac.kopo.lyh.personalcolor.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 파일 저장 서비스 - Spring Boot 3.4 최적화
 * Path API 및 보안 강화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ApplicationConfig.FileStorageProperties fileStorageProperties;

    /**
     * 업로드 디렉토리 초기화
     */
    public void init() {
        try {
            Path uploadPath = getUploadPath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new FileStorageException("업로드 디렉토리 생성 실패", e);
        }
    }

    /**
     * 파일 저장
     */
    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFilename = generateStoredFilename(originalFilename);

        try {
            // 경로 순회 공격 방지
            if (originalFilename.contains("..")) {
                throw new FileStorageException("파일명에 상위 경로 참조가 포함되어 있습니다: " + originalFilename);
            }

            Path targetLocation = getUploadPath().resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {} -> {}", originalFilename, storedFilename);
            return storedFilename;

        } catch (IOException e) {
            throw new FileStorageException("파일 저장 실패: " + originalFilename, e);
        }
    }

    /**
     * 파일 로드
     */
    public Resource loadFile(String filename) {
        try {
            Path filePath = getUploadPath().resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("파일을 찾을 수 없거나 읽을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("파일 로드 실패: " + filename, e);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String filename) {
        try {
            Path filePath = getUploadPath().resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", filename);
        } catch (IOException e) {
            throw new FileStorageException("파일 삭제 실패: " + filename, e);
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean exists(String filename) {
        Path filePath = getUploadPath().resolve(filename).normalize();
        return Files.exists(filePath);
    }

    /**
     * 파일 크기 반환
     */
    public long getFileSize(String filename) {
        try {
            Path filePath = getUploadPath().resolve(filename).normalize();
            return Files.size(filePath);
        } catch (IOException e) {
            throw new FileStorageException("파일 크기 조회 실패: " + filename, e);
        }
    }

    // === 내부 메서드들 ===

    private Path getUploadPath() {
        return Paths.get(fileStorageProperties.uploadDir()).toAbsolutePath().normalize();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("빈 파일은 저장할 수 없습니다.");
        }

        if (file.getSize() > fileStorageProperties.maxFileSize()) {
            throw new FileStorageException("파일 크기가 허용된 최대 크기를 초과합니다: " + file.getSize());
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new FileStorageException("파일명이 없습니다.");
        }

        String extension = getFileExtension(filename);
        if (!isAllowedExtension(extension)) {
            throw new FileStorageException("허용되지 않는 파일 형식입니다: " + extension);
        }
    }

    private String generateStoredFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s_%s%s", timestamp, uuid, extension);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        return Arrays.asList(fileStorageProperties.allowedExtensions()).contains(extension);
    }
}