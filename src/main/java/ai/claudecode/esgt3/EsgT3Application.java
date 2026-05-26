package ai.claudecode.esgt3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EsgT3Application {

    public static void main(String[] args) {
        SpringApplication.run(EsgT3Application.class, args);
    }
}
