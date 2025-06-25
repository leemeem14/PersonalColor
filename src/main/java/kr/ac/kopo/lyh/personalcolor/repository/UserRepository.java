package kr.ac.kopo.lyh.personalcolor.repository;

import kr.ac.kopo.lyh.personalcolor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository - Spring Data JPA 3.4
 * Query Methods 및 Custom Queries 활용
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 활성화된 사용자 조회
     */
    List<User> findByEnabledTrue();

    /**
     * 생성일 범위로 사용자 조회
     */
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 성별로 사용자 조회
     */
    List<User> findByGender(User.Gender gender);

    /**
     * 이름으로 사용자 검색 (LIKE 검색)
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.enabled = true")
    List<User> findByNameContainingAndEnabledTrue(@Param("name") String name);

    /**
     * 사용자 통계 - 성별별 카운트
     */
    @Query("SELECT u.gender, COUNT(u) FROM User u WHERE u.enabled = true GROUP BY u.gender")
    List<Object[]> countUsersByGender();

    /**
     * 최근 가입한 사용자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true ORDER BY u.createdAt DESC")
    Page<User> findRecentUsers(Pageable pageable);

    /**
     * 특정 기간 동안 가입한 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countUsersByPeriod(@Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);

    /**
     * 컬러 분석을 수행한 사용자들 조회
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.colorAnalyses ca WHERE u.enabled = true")
    List<User> findUsersWithColorAnalyses();

    /**
     * 사용자 계정 비활성화
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = false, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int deactivateUser(@Param("userId") Long userId);

    /**
     * 사용자 마지막 로그인 시간 업데이트 (추가 필드가 있다면)
     */
    @Modifying
    @Query("UPDATE User u SET u.updatedAt = CURRENT_TIMESTAMP WHERE u.email = :email")
    int updateLastLoginTime(@Param("email") String email);
}