package kr.ac.kopo.lyh.personalcolor.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PersonalColor 프로젝트의 전역 예외 처리 핸들러
 *
 * Spring Boot 3.4.0과 Jakarta EE 기반으로 작성됨
 * 모든 예외를 중앙에서 처리하여 일관된 API 응답을 제공
 *
 * @author PersonalColor Team
 * @version 2.0
 * @since Spring Boot 3.4.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * AnalysisNotFoundException 처리
     * 퍼스널 컬러 분석 결과를 찾을 수 없을 때 발생
     */
    @ExceptionHandler(AnalysisNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAnalysisNotFoundException(
            AnalysisNotFoundException ex,
            HttpServletRequest request,
            WebRequest webRequest) {

        String errorId = generateErrorId();

        logger.warn("🔍 분석 결과 조회 실패 [ID: {}] - 분석 ID: {}, 사용자 ID: {}, 메시지: {}",
                errorId, ex.getAnalysisId(), ex.getUserId(), ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "ANALYSIS_NOT_FOUND",
                ex.getDetailedMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                Map.of(
                        "analysisId", ex.getAnalysisId() != null ? ex.getAnalysisId() : "unknown",
                        "userId", ex.getUserId() != null ? ex.getUserId() : "unknown",
                        "suggestion", "분석 ID를 확인하거나 새로운 퍼스널 컬러 분석을 요청해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * FileStorageException 처리
     * 파일 저장, 로드, 삭제 중 발생하는 예외
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorageException(
            FileStorageException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.error("📁 파일 저장소 오류 [ID: {}] - 메시지: {}", errorId, ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "FILE_STORAGE_ERROR",
                "파일 처리 중 오류가 발생했습니다: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                Map.of(
                        "suggestion", "파일 크기와 형식을 확인하고 다시 시도해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * UnauthorizedAccessException 처리
     * 인증되지 않은 접근 시도
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccessException(
            UnauthorizedAccessException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.warn("🚫 인증되지 않은 접근 시도 [ID: {}] - URI: {}, 메시지: {}",
                errorId, request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "UNAUTHORIZED_ACCESS",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                Map.of(
                        "suggestion", "로그인이 필요합니다. 인증 정보를 확인해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * UserNotFoundException 처리
     * 사용자 정보를 찾을 수 없을 때
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
            UserNotFoundException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.warn("👤 사용자 조회 실패 [ID: {}] - 메시지: {}", errorId, ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "USER_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                Map.of(
                        "suggestion", "사용자 ID를 확인하거나 회원가입을 진행해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * DuplicateEmailException 처리
     * 이메일 중복 등록 시도
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmailException(
            DuplicateEmailException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.warn("📧 이메일 중복 오류 [ID: {}] - 메시지: {}", errorId, ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "DUPLICATE_EMAIL",
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                Map.of(
                        "suggestion", "다른 이메일 주소를 사용하거나 기존 계정으로 로그인해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Spring Security AuthenticationException 처리
     * 인증 실패
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.warn("🔐 인증 실패 [ID: {}] - 메시지: {}", errorId, ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "AUTHENTICATION_FAILED",
                "인증에 실패했습니다: " + ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                Map.of(
                        "suggestion", "사용자명과 비밀번호를 확인해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Spring Security AccessDeniedException 처리
     * 권한 부족
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.warn("🛡️ 접근 권한 부족 [ID: {}] - URI: {}, 메시지: {}",
                errorId, request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "ACCESS_DENIED",
                "접근 권한이 부족합니다: " + ex.getMessage(),
                HttpStatus.FORBIDDEN,
                request.getRequestURI(),
                Map.of(
                        "suggestion", "필요한 권한을 확인하고 관리자에게 문의하세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Validation 예외 처리 (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.warn("✅ 입력 값 검증 실패 [ID: {}] - 필드 오류 개수: {}",
                errorId, ex.getBindingResult().getFieldErrorCount());

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "VALIDATION_FAILED",
                "입력 값 검증에 실패했습니다.",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                Map.of(
                        "fieldErrors", fieldErrors,
                        "suggestion", "입력 형식을 확인하고 올바른 값을 입력해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Constraint Validation 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.warn("🚨 제약 조건 위반 [ID: {}] - 메시지: {}", errorId, ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "CONSTRAINT_VIOLATION",
                "제약 조건을 위반했습니다: " + ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                Map.of(
                        "suggestion", "입력 값이 허용된 조건을 만족하는지 확인해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 파일 업로드 크기 초과 예외 처리
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.warn("📄 파일 크기 초과 [ID: {}] - 최대 크기: {}", errorId, ex.getMaxUploadSize());

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "FILE_SIZE_EXCEEDED",
                "업로드 파일 크기가 허용된 최대 크기를 초과했습니다.",
                HttpStatus.PAYLOAD_TOO_LARGE,
                request.getRequestURI(),
                Map.of(
                        "maxSize", ex.getMaxUploadSize() + " bytes",
                        "suggestion", "파일 크기를 줄이거나 여러 파일로 나누어 업로드해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * 데이터베이스 무결성 위반 예외 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.error("🗄️ 데이터 무결성 위반 [ID: {}] - 메시지: {}", errorId, ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "DATA_INTEGRITY_VIOLATION",
                "데이터 무결성 규칙을 위반했습니다.",
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                Map.of(
                        "suggestion", "중복된 데이터가 있는지 확인하고 다시 시도해주세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * 일반적인 모든 예외 처리 (최종 대응)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        String errorId = generateErrorId();

        logger.error("💥 예상치 못한 오류 발생 [ID: {}] - 타입: {}, 메시지: {}",
                errorId, ex.getClass().getSimpleName(), ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
                errorId,
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                Map.of(
                        "exceptionType", ex.getClass().getSimpleName(),
                        "suggestion", "잠시 후 다시 시도해주세요. 문제가 지속되면 관리자에게 문의하세요."
                )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 표준화된 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(
            String errorId,
            String errorCode,
            String message,
            HttpStatus status,
            String path,
            Map<String, Object> additionalInfo) {

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        errorResponse.put("errorId", errorId);
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("path", path);

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            errorResponse.put("details", additionalInfo);
        }

        return errorResponse;
    }

    /**
     * 고유 에러 ID 생성 (디버깅 및 추적용)
     */
    private String generateErrorId() {
        return "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
