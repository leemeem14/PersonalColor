package kr.ac.kopo.lyh.personalcolor.exception;

/**
 * 인증되지 않은 접근 시 발생하는 예외 클래스
 * Spring Boot 3.4.0 호환
 *
 * @author PersonalColor Team
 * @since 2025.06.23
 */
public class UnauthorizedAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 기본 생성자
     * 기본 메시지로 예외 생성
     */
    public UnauthorizedAccessException() {
        super("권한이 없는 접근입니다.");
    }

    /**
     * 메시지를 포함한 생성자
     *
     * @param message 예외 메시지
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인을 포함한 생성자
     *
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인만 포함한 생성자
     *
     * @param cause 예외 원인
     */
    public UnauthorizedAccessException(Throwable cause) {
        super("권한이 없는 접근입니다.", cause);
    }
}