package net.shyshkin.study.batch.performance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class HighPerformanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HighPerformanceApplication.class, args);
    }

}
