package kr.ac.kopo.lyh.personalcolor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Application Configuration for Spring Boot 3.4
 * Virtual Threads 및 비동기 처리 설정
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ApplicationConfig.FileStorageProperties.class)
public class ApplicationConfig {

    /**
     * Virtual Threads를 사용한 비동기 작업 실행자
     * Spring Boot 3.4에서 Virtual Threads 지원
     */
    @Bean(name = "taskExecutor")
    @Primary
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("PersonalColor-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        // Security Context를 비동기 작업에 전파
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

    /**
     * Virtual Threads 기반 스케줄러 (Spring Boot 3.4+)
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("PersonalColor-Scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

    /**
     * Jackson ObjectMapper 설정
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * 파일 저장소 설정 프로퍼티
     */
    @ConfigurationProperties(prefix = "app.file-storage")
    public record FileStorageProperties(
            String uploadDir,
            long maxFileSize,
            String[] allowedExtensions
    ) {
        public FileStorageProperties() {
            this("uploads", 10485760L, new String[]{".jpg", ".jpeg", ".png", ".gif", ".bmp"});
        }
    }
}