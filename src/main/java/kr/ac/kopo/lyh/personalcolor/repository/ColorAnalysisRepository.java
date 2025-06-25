package kr.ac.kopo.lyh.personalcolor.repository;

import kr.ac.kopo.lyh.personalcolor.entity.ColorAnalysis;
import kr.ac.kopo.lyh.personalcolor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 컬러 분석 Repository - Spring Data JPA 3.4
 * 복잡한 쿼리 및 통계 조회 기능 포함
 */
@Repository
public interface ColorAnalysisRepository extends JpaRepository<ColorAnalysis, Long> {

    /**
     * 사용자별 분석 결과 조회 (최신순)
     */
    List<ColorAnalysis> findByUserOrderByAnalyzedAtDesc(User user);

    /**
     * 사용자별 분석 결과 페이징 조회
     */
    Page<ColorAnalysis> findByUserOrderByAnalyzedAtDesc(User user, Pageable pageable);

    /**
     * 사용자의 가장 최근 분석 결과
     */
    Optional<ColorAnalysis> findFirstByUserOrderByAnalyzedAtDesc(User user);

    /**
     * 특정 컬러 타입의 분석 결과들
     */
    List<ColorAnalysis> findByColorType(ColorAnalysis.ColorType colorType);

    /**
     * 신뢰도가 특정 값 이상인 분석 결과들
     */
    List<ColorAnalysis> findByConfidenceGreaterThanEqual(BigDecimal minConfidence);

    /**
     * 사용자별 특정 기간 분석 결과
     */
    @Query("SELECT ca FROM ColorAnalysis ca WHERE ca.user = :user " +
            "AND ca.analyzedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY ca.analyzedAt DESC")
    List<ColorAnalysis> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 컬러 타입별 통계
     */
    @Query("SELECT ca.colorType, COUNT(ca) FROM ColorAnalysis ca GROUP BY ca.colorType")
    List<Object[]> getColorTypeStatistics();

    /**
     * 사용자별 분석 횟수
     */
    @Query("SELECT COUNT(ca) FROM ColorAnalysis ca WHERE ca.user = :user")
    Long countByUser(@Param("user") User user);

    /**
     * 월별 분석 통계
     */
    @Query("SELECT YEAR(ca.analyzedAt), MONTH(ca.analyzedAt), COUNT(ca) " +
            "FROM ColorAnalysis ca " +
            "WHERE ca.analyzedAt >= :startDate " +
            "GROUP BY YEAR(ca.analyzedAt), MONTH(ca.analyzedAt) " +
            "ORDER BY YEAR(ca.analyzedAt), MONTH(ca.analyzedAt)")
    List<Object[]> getMonthlyAnalysisStatistics(@Param("startDate") LocalDateTime startDate);

    /**
     * 높은 신뢰도 분석 결과들 (상위 10개)
     */
    @Query("SELECT ca FROM ColorAnalysis ca " +
            "WHERE ca.confidence >= :minConfidence " +
            "ORDER BY ca.confidence DESC, ca.analyzedAt DESC")
    Page<ColorAnalysis> findTopByConfidence(@Param("minConfidence") BigDecimal minConfidence,
                                            Pageable pageable);

    /**
     * 특정 파일명의 분석 결과 존재 여부
     */
    boolean existsByStoredFileName(String storedFileName);

    /**
     * 사용자의 최다 분석된 컬러 타입
     */
    @Query("SELECT ca.colorType FROM ColorAnalysis ca " +
            "WHERE ca.user = :user " +
            "GROUP BY ca.colorType " +
            "ORDER BY COUNT(ca.colorType) DESC")
    List<ColorAnalysis.ColorType> findMostFrequentColorTypeByUser(@Param("user") User user);

    /**
     * 전체 평균 신뢰도
     */
    @Query("SELECT AVG(ca.confidence) FROM ColorAnalysis ca")
    BigDecimal getAverageConfidence();

    /**
     * 특정 기간 동안의 분석 결과 수
     */
    @Query("SELECT COUNT(ca) FROM ColorAnalysis ca " +
            "WHERE ca.analyzedAt BETWEEN :startDate AND :endDate")
    long countAnalysesByPeriod(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 컬러 타입별 분석 횟수
     */
    @Query("SELECT ca.colorType, COUNT(ca) FROM ColorAnalysis ca " +
            "WHERE ca.user = :user " +
            "GROUP BY ca.colorType " +
            "ORDER BY COUNT(ca) DESC")
    List<Object[]> getUserColorTypeStatistics(@Param("user") User user);

    /**
     * 최근 N일간의 분석 결과
     */
    @Query("SELECT ca FROM ColorAnalysis ca " +
            "WHERE ca.analyzedAt >= :sinceDate " +
            "ORDER BY ca.analyzedAt DESC")
    List<ColorAnalysis> findRecentAnalyses(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * 특정 사용자의 평균 신뢰도
     */
    @Query("SELECT AVG(ca.confidence) FROM ColorAnalysis ca WHERE ca.user = :user")
    BigDecimal getAverageConfidenceByUser(@Param("user") User user);
}