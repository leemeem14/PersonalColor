package kr.ac.kopo.lyh.personalcolor.exception;

import kr.ac.kopo.lyh.personalcolor.exception.DuplicateEmailException;
import kr.ac.kopo.lyh.personalcolor.exception.FileStorageException;
import kr.ac.kopo.lyh.personalcolor.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * Spring Boot 3.4.0 호환
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 사용자를 찾을 수 없는 경우의 예외 처리
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException e) {
        log.error("사용자를 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
    }

    /**
     * 이메일 중복 예외 처리
     */
    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleDuplicateEmailException(DuplicateEmailException e) {
        log.error("이메일 중복: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("success", false, "error", e.getMessage()));
    }

    /**
     * 파일 저장 관련 예외 처리
     */
    @ExceptionHandler(FileStorageException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleFileStorageException(FileStorageException e) {
        log.error("파일 처리 오류: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
    }

    /**
     * 파일 크기 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("파일 크기 초과: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", "파일 크기가 너무 큽니다. (최대 10MB)"));
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("서버 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "서버 내부 오류가 발생했습니다."));
    }
}