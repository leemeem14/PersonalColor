package kr.ac.kopo.lyh.personalcolor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * PersonalColor Spring Boot Application
 * Spring Boot 3.4 / Spring Framework 6.1 기반
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
public class PersonalColorApplication {

    public static void main(String[] args) {
        // Virtual Threads 활성화 (JDK 21+)
        System.setProperty("spring.threads.virtual.enabled", "true");
        SpringApplication.run(PersonalColorApplication.class, args);
    }
}