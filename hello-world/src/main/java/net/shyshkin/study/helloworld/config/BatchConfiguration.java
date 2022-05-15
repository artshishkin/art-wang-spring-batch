package net.shyshkin.study.helloworld.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.helloworld.listener.HelloWorldJobExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final HelloWorldJobExecutionListener hwListener;

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
    public Job helloWorldJob() {
        return jobs.get("helloWorldJob")
                .listener(hwListener)
                .start(step1())
                .build();
    }

}
