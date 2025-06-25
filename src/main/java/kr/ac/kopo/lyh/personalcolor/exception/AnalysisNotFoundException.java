package kr.ac.kopo.lyh.personalcolor.exception;

/**
 * PersonalColor 프로젝트에서 퍼스널 컬러 분석 결과를 찾을 수 없을 때 발생하는 예외
 *
 * Spring Boot 3.4.0과 완전 호환되며, Jakarta EE 기반으로 작성됨
 *
 * @author PersonalColor Team
 * @version 1.0
 * @since Spring Boot 3.4.0
 */
public class AnalysisNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String analysisId;
    private final String userId;

    /**
     * 기본 생성자 - 메시지만 포함
     *
     * @param message 예외 메시지
     */
    public AnalysisNotFoundException(String message) {
        super(message);
        this.analysisId = null;
        this.userId = null;
    }

    /**
     * 분석 ID와 함께 예외를 생성
     *
     * @param message 예외 메시지
     * @param analysisId 찾을 수 없는 분석 ID
     */
    public AnalysisNotFoundException(String message, String analysisId) {
        super(message);
        this.analysisId = analysisId;
        this.userId = null;
    }

    /**
     * 사용자 ID와 분석 ID를 모두 포함하는 생성자
     *
     * @param message 예외 메시지
     * @param analysisId 찾을 수 없는 분석 ID
     * @param userId 관련 사용자 ID
     */
    public AnalysisNotFoundException(String message, String analysisId, String userId) {
        super(message);
        this.analysisId = analysisId;
        this.userId = userId;
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public AnalysisNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.analysisId = null;
        this.userId = null;
    }

    /**
     * 모든 정보를 포함하는 완전한 생성자
     *
     * @param message 예외 메시지
     * @param analysisId 분석 ID
     * @param userId 사용자 ID
     * @param cause 원인 예외
     */
    public AnalysisNotFoundException(String message, String analysisId, String userId, Throwable cause) {
        super(message, cause);
        this.analysisId = analysisId;
        this.userId = userId;
    }

    /**
     * 예외 체인 처리를 위한 보호된 생성자
     */
    protected AnalysisNotFoundException(String message, Throwable cause,
                                        boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.analysisId = null;
        this.userId = null;
    }

    /**
     * 분석 ID를 반환
     *
     * @return 분석 ID (없을 경우 null)
     */
    public String getAnalysisId() {
        return analysisId;
    }

    /**
     * 사용자 ID를 반환
     *
     * @return 사용자 ID (없을 경우 null)
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 상세한 에러 정보를 포함한 메시지를 반환
     *
     * @return 구조화된 에러 메시지
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder(getMessage());

        if (analysisId != null) {
            sb.append(" [Analysis ID: ").append(analysisId).append("]");
        }

        if (userId != null) {
            sb.append(" [User ID: ").append(userId).append("]");
        }

        return sb.toString();
    }

    /**
     * 디버깅을 위한 toString 메서드 오버라이드
     */
    @Override
    public String toString() {
        return "AnalysisNotFoundException{" +
                "message='" + getMessage() + '\'' +
                ", analysisId='" + analysisId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }}