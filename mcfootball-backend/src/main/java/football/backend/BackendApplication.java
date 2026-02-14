package football.backend;

import football.backend.config.ApiFetchConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MCFootball Backend — Spring Boot entry point.
 * <p>
 * Provides REST endpoints to fetch match data, validate with CoCos,
 * and write .fb model files for the mcfootball-generator to consume.
 */
@SpringBootApplication
@EnableConfigurationProperties(ApiFetchConfig.class)
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
