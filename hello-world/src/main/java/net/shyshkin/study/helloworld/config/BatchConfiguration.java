package net.shyshkin.study.helloworld.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.helloworld.listener.HelloWorldJobExecutionListener;
import net.shyshkin.study.helloworld.listener.HelloWorldStepExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final HelloWorldJobExecutionListener hwJobListener;
    private final HelloWorldStepExecutionListener hwStepListener;

    @Bean
    public Step step1() {
        return steps.get("step1")
                .listener(hwStepListener)
                .tasklet(helloWorldTasklet())
                .build();
    }

    private Tasklet helloWorldTasklet() {
        return (stepContribution, chunkContext) -> {
            var my_name = Optional.ofNullable(stepContribution.getStepExecution().getJobExecution().getExecutionContext().get("my name"));
            log.debug("Hello {}", my_name.orElse("World"));
            log.debug("Job parameters: {}", stepContribution.getStepExecution().getJobExecution().getJobParameters());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Job helloWorldJob() {
        return jobs.get("helloWorldJob")
                .listener(hwJobListener)
                .start(step1())
                .build();
    }

}
