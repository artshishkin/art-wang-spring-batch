package net.shyshkin.study.helloworld;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class HelloWorldApplication {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }

    @Bean
    public Step step1() {
        return steps.get("step1")
                .tasklet(helloWorldTasklet())
                .build();
    }

    private Tasklet helloWorldTasklet() {
        return (stepContribution, chunkContext) -> {
            log.debug("Hello World");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Job helloWorldJob(){
        return jobs.get("helloWorldJob")
                .start(step1())
                .build();
    }

}
