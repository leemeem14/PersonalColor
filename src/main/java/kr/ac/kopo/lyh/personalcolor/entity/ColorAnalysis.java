package kr.ac.kopo.lyh.personalcolor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.type.descriptor.java.StringJavaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 컬러 분석 결과 엔티티
 * Jakarta EE 10 및 JPA 최적화
 */
@Entity
@Table(name = "color_analyses",
        indexes = {
                @Index(name = "idx_analysis_user_date", columnList = "user_id, analyzedAt"),
                @Index(name = "idx_analysis_color_type", columnList = "colorType"),
                @Index(name = "idx_analysis_created", columnList = "analyzedAt")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(of = "id")
public class ColorAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_analysis_user"))
    @NotNull(message = "사용자 정보는 필수입니다")
    private User user;

    @NotBlank(message = "원본 파일명은 필수입니다")
    @Column(nullable = false, length = 255)
    private String originalFileName;

    @NotBlank(message = "저장된 파일명은 필수입니다")
    @Column(nullable = false, length = 255)
    private String storedFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "컬러 타입은 필수입니다")
    private ColorType colorType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.0", message = "신뢰도는 0.0 이상이어야 합니다")
    @DecimalMax(value = "1.0", message = "신뢰도는 1.0 이하여야 합니다")
    @Column(precision = 5, scale = 4, nullable = false)
    @Builder.Default
    private BigDecimal confidence = BigDecimal.ZERO;

    // JSON 형태로 저장되는 색상 정보
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String recommendedColors;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    // 메타데이터
    @Column(length = 100)
    private String imageWidth;

    @Column(length = 100)
    private String imageHeight;

    @Column(length = 50)
    private String fileSize;

    @Column(length = 20)
    private String contentType;

    // 컬러 타입 enum
    public enum ColorType {
        SPRING_WARM("봄 웜톤", "밝고 따뜻한 톤"),
        SUMMER_COOL("여름 쿨톤", "부드럽고 시원한 톤"),
        AUTUMN_WARM("가을 웜톤", "깊고 따뜻한 톤"),
        WINTER_COOL("겨울 쿨톤", "선명하고 시원한 톤"),
        NEUTRAL("중성톤", "웜톤과 쿨톤의 중간");

        private final String displayName;
        private final String description;

        ColorType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    // 편의 메서드
    public void setUser(User user) {
        this.user = user;
        if (user != null && !user.getColorAnalyses().contains(this)) {
            user.getColorAnalyses().add(this);
        }
    }

    /**
     * 신뢰도를 퍼센트로 반환
     */
    public int getConfidencePercent() {
        return confidence.multiply(BigDecimal.valueOf(100)).intValue();
    }

    /**
     * 분석 결과가 신뢰할 만한지 판단
     */
    public boolean isReliable() {
        return confidence.compareTo(BigDecimal.valueOf(0.7)) >= 0;
    }
}