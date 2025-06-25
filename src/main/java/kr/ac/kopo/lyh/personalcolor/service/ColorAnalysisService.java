package kr.ac.kopo.lyh.personalcolor.service;

import kr.ac.kopo.lyh.personalcolor.entity.ColorAnalysis;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import kr.ac.kopo.lyh.personalcolor.exception.AnalysisNotFoundException;
import kr.ac.kopo.lyh.personalcolor.exception.UnauthorizedAccessException;
import kr.ac.kopo.lyh.personalcolor.repository.ColorAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 컬러 분석 서비스 - Spring Boot 3.4 최적화
 * 비동기 처리 및 Virtual Threads 활용
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ColorAnalysisService {

    private final ColorAnalysisRepository colorAnalysisRepository;
    private final FileStorageService fileStorageService;

    /**
     * 이미지 분석 수행 (비동기)
     */
    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<ColorAnalysis> analyzeImageAsync(User user, String originalFileName, String storedFileName) {
        return CompletableFuture.supplyAsync(() -> analyzeImage(user, originalFileName, storedFileName));
    }

    /**
     * 이미지 분석 수행
     */
    @Transactional
    public ColorAnalysis analyzeImage(User user, String originalFileName, String storedFileName) {
        log.info("이미지 분석 시작 - 사용자: {}, 파일: {}", user.getEmail(), originalFileName);

        try {
            // 실제 AI 분석 로직 (현재는 샘플 구현)
            ColorAnalysis.ColorType colorType = performColorAnalysis(storedFileName);
            BigDecimal confidence = calculateConfidence(colorType);
            String description = generateDescription(colorType);
            String recommendedColors = generateRecommendedColors(colorType);

            // 분석 결과 저장
            ColorAnalysis analysis = ColorAnalysis.builder()
                    .user(user)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .colorType(colorType)
                    .confidence(confidence)
                    .description(description)
                    .recommendedColors(recommendedColors)
                    .build();

            ColorAnalysis savedAnalysis = colorAnalysisRepository.save(analysis);

            log.info("이미지 분석 완료 - ID: {}, 컬러타입: {}, 신뢰도: {}%",
                    savedAnalysis.getId(), colorType.getDisplayName(), confidence.multiply(BigDecimal.valueOf(100)).intValue());

            return savedAnalysis;

        } catch (Exception e) {
            log.error("이미지 분석 실패 - 사용자: {}, 파일: {}", user.getEmail(), originalFileName, e);
            throw new RuntimeException("이미지 분석 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자별 분석 결과 조회
     */
    public List<ColorAnalysis> getUserAnalyses(User user) {
        return colorAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user);
    }

    /**
     * 사용자별 분석 결과 페이징 조회
     */
    public Page<ColorAnalysis> getUserAnalyses(User user, Pageable pageable) {
        return colorAnalysisRepository.findByUserOrderByAnalyzedAtDesc(user, pageable);
    }

    /**
     * 사용자의 최근 분석 결과 조회
     */
    public ColorAnalysis getLatestAnalysis(User user) {
        return colorAnalysisRepository.findFirstByUserOrderByAnalyzedAtDesc(user)
                .orElse(null);
    }

    /**
     * 분석 결과 상세 조회
     */
    public ColorAnalysis getAnalysisById(Long analysisId) {
        return colorAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new AnalysisNotFoundException("분석 결과를 찾을 수 없습니다: " + analysisId));
    }

    /**
     * 분석 결과 삭제
     */
    @Transactional
    public void deleteAnalysis(Long analysisId, User user) {
        ColorAnalysis analysis = getAnalysisById(analysisId);

        if (!analysis.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("삭제 권한이 없습니다.");
        }

        // 파일도 함께 삭제
        try {
            fileStorageService.deleteFile(analysis.getStoredFileName());
        } catch (Exception e) {
            log.warn("파일 삭제 실패: {}", analysis.getStoredFileName(), e);
        }

        colorAnalysisRepository.delete(analysis);
        log.info("분석 결과 삭제 완료 - ID: {}, 사용자: {}", analysisId, user.getEmail());
    }

    /**
     * 컬러 타입별 통계
     */
    public Map<ColorAnalysis.ColorType, Long> getColorTypeStatistics() {
        List<Object[]> results = colorAnalysisRepository.getColorTypeStatistics();
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        result -> (ColorAnalysis.ColorType) result[0],
                        result -> (Long) result[1]
                ));
    }

    /**
     * 사용자별 분석 횟수
     */
    public Long getUserAnalysisCount(User user) {
        return colorAnalysisRepository.countByUser(user);
    }

    /**
     * 전체 평균 신뢰도
     */
    public BigDecimal getAverageConfidence() {
        return colorAnalysisRepository.getAverageConfidence();
    }

    /**
     * 사용자별 평균 신뢰도
     */
    public BigDecimal getUserAverageConfidence(User user) {
        return colorAnalysisRepository.getAverageConfidenceByUser(user);
    }

    // === 내부 분석 메서드들 ===

    /**
     * 실제 컬러 분석 로직 (AI 모델 연동 부분)
     */
    private ColorAnalysis.ColorType performColorAnalysis(String fileName) {
        // TODO: 실제 AI 모델과 연동하여 분석 수행
        // 현재는 샘플 구현

        // 파일명 해시를 기반으로 랜덤한 컬러 타입 결정 (데모용)
        int hash = fileName.hashCode();
        ColorAnalysis.ColorType[] types = ColorAnalysis.ColorType.values();
        return types[Math.abs(hash) % types.length];
    }

    /**
     * 신뢰도 계산
     */
    private BigDecimal calculateConfidence(ColorAnalysis.ColorType colorType) {
        // TODO: 실제 AI 모델의 confidence score 사용
        // 현재는 0.7 ~ 0.95 사이의 랜덤값
        double confidence = 0.7 + (Math.random() * 0.25);
        return BigDecimal.valueOf(confidence).setScale(4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 컬러 타입별 설명 생성
     */
    private String generateDescription(ColorAnalysis.ColorType colorType) {
        return switch (colorType) {
            case SPRING_WARM -> "밝고 화사한 웜톤으로, 생동감 있는 색상이 잘 어울립니다. " +
                    "코랄, 피치, 연한 골드 톤의 색상을 추천합니다.";
            case SUMMER_COOL -> "부드럽고 우아한 쿨톤으로, 파스텔과 실버 톤이 잘 어울립니다. " +
                    "라벤더, 로즈, 민트 계열의 색상을 추천합니다.";
            case AUTUMN_WARM -> "깊고 따뜻한 웜톤으로, 어스 톤 계열이 잘 어울립니다. " +
                    "브라운, 오렌지, 딥 골드 계열의 색상을 추천합니다.";
            case WINTER_COOL -> "선명하고 강렬한 쿨톤으로, 대비가 뚜렷한 색상이 잘 어울립니다. " +
                    "네이비, 레드, 실버 계열의 색상을 추천합니다.";
            case NEUTRAL -> "웜톤과 쿨톤의 특성을 모두 가진 중성톤으로, 다양한 색상을 소화할 수 있습니다.";
        };
    }

    /**
     * 추천 색상 JSON 생성
     */
    private String generateRecommendedColors(ColorAnalysis.ColorType colorType) {
        return switch (colorType) {
            case SPRING_WARM -> """
                {
                    "primary": ["#FFB6C1", "#FFA07A", "#F0E68C", "#98FB98"],
                    "secondary": ["#FF6347", "#FFD700", "#ADFF2F", "#FF69B4"],
                    "accent": ["#FF4500", "#DAA520", "#32CD32"]
                }""";
            case SUMMER_COOL -> """
                {
                    "primary": ["#E6E6FA", "#B0C4DE", "#F0F8FF", "#DDA0DD"],
                    "secondary": ["#9370DB", "#87CEEB", "#98FB98", "#F0E68C"],
                    "accent": ["#6A5ACD", "#4682B4", "#00CED1"]
                }""";
            case AUTUMN_WARM -> """
                {
                    "primary": ["#D2691E", "#CD853F", "#B22222", "#8B4513"],
                    "secondary": ["#A0522D", "#BC8F8F", "#F4A460", "#DEB887"],
                    "accent": ["#8B0000", "#FF6347", "#DAA520"]
                }""";
            case WINTER_COOL -> """
                {
                    "primary": ["#000080", "#800080", "#DC143C", "#008B8B"],
                    "secondary": ["#4B0082", "#2F4F4F", "#8B008B", "#00008B"],
                    "accent": ["#FF1493", "#0000CD", "#8A2BE2"]
                }""";
            case NEUTRAL -> """
                {
                    "primary": ["#808080", "#A9A9A9", "#C0C0C0", "#D3D3D3"],
                    "secondary": ["#696969", "#778899", "#B0C4DE", "#F5F5DC"],
                    "accent": ["#2F4F4F", "#708090", "#556B2F"]
                }""";
        };
    }
}
