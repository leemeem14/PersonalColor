package kr.ac.kopo.lyh.personalcolor.service;

import kr.ac.kopo.lyh.personalcolor.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 파일 저장, 로드, 삭제 등의 파일 관리를 담당하는 서비스 클래스
 * Spring Boot 3.4.0 호환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.max.size:10485760}") // 10MB 기본값
    private long maxFileSize;

    @Value("#{'${file.allowed.extensions:.jpg,.jpeg,.png,.gif,.bmp,.webp}'.split(',')}")
    private List<String> allowedExtensions;

    /**
     * 서비스 초기화 시 업로드 디렉토리 생성
     */
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = getUploadPath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성됨: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new FileStorageException("업로드 디렉토리 생성 실패", e);
        }
    }

    /**
     * 파일 저장
     * @param file 저장할 파일
     * @return 저장된 파일명
     */
    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFilename = generateStoredFilename(originalFilename);

        try {
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
     * @param filename 로드할 파일명
     * @return 파일 리소스
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
     * @param filename 삭제할 파일명
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
     * @param filename 확인할 파일명
     * @return 파일 존재 여부
     */
    public boolean exists(String filename) {
        Path filePath = getUploadPath().resolve(filename).normalize();
        return Files.exists(filePath);
    }

    /**
     * 파일 크기 반환
     * @param filename 파일명
     * @return 파일 크기 (바이트)
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

    /**
     * 업로드 경로 반환
     */
    private Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("빈 파일은 저장할 수 없습니다.");
        }

        if (file.getSize() > maxFileSize) {
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

    /**
     * 저장될 파일명 생성 (중복 방지)
     */
    private String generateStoredFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s_%s%s", timestamp, uuid, extension);
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }

    /**
     * 허용된 확장자인지 확인
     */
    private boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension);
    }
}