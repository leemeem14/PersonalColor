package kr.ac.kopo.lyh.personalcolor.config;

import kr.ac.kopo.lyh.personalcolor.exception.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Spring Security 설정 클래스
 * Spring Boot 3.4.0 호환 버전
 *
 * @author PersonalColor Team
 * @since 2025.06.23
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Security Filter Chain 설정
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (API 서버용)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 설정 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 퍼블릭 엔드포인트 허용
                        .requestMatchers(
                                "/api/public/**",
                                "/api/auth/**",
                                "/api/color-analysis/**",
                                "/h2-console/**",
                                "/error",
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**"
                        ).permitAll()

                        // 관리자 전용 엔드포인트
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 사용자 엔드포인트
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 예외 처리 설정
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                )

                // H2 Console 프레임 옵션 비활성화 (개발 환경용)
                .headers(headers -> headers
                        .frameOptions().sameOrigin()
                );

        return http.build();
    }

    /**
     * CORS 설정
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정 (개발환경)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 자격 증명 허용
        configuration.setAllowCredentials(true);

        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Cache-Control", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 커스텀 인증 진입점
     * 인증되지 않은 요청에 대한 처리
     *
     * @return AuthenticationEntryPoint
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response,
                AuthenticationException authException) -> {

            log.error("인증되지 않은 접근 시도: {} - {}",
                    request.getRequestURI(), authException.getMessage());

            // JSON 응답 설정
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

            // 에러 응답 생성
            String jsonResponse = String.format("""
                {
                    "timestamp": "%s",
                    "status": %d,
                    "error": "Unauthorized",
                    "message": "인증이 필요합니다.",
                    "path": "%s"
                }
                """,
                    java.time.LocalDateTime.now(),
                    HttpStatus.UNAUTHORIZED.value(),
                    request.getRequestURI()
            );

            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        };
    }

    /**
     * 커스텀 접근 거부 핸들러
     * 권한이 없는 요청에 대한 처리
     *
     * @return AccessDeniedHandler
     */
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                AccessDeniedException accessDeniedException) -> {

            log.error("접근 권한 없음: {} - {}",
                    request.getRequestURI(), accessDeniedException.getMessage());

            // JSON 응답 설정
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpStatus.FORBIDDEN.value());

            // 에러 응답 생성
            String jsonResponse = String.format("""
                {
                    "timestamp": "%s",
                    "status": %d,
                    "error": "Forbidden",
                    "message": "해당 리소스에 접근할 권한이 없습니다.",
                    "path": "%s"
                }
                """,
                    java.time.LocalDateTime.now(),
                    HttpStatus.FORBIDDEN.value(),
                    request.getRequestURI()
            );

            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        };
    }
}