package net.shyshkin.study.batch.performance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.performance.tasklet.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
@Profile("parallel-flow")
public class ParallelFlowBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    private final DownloadTasklet downloadTasklet;
    private final FileProcessTasklet fileProcessTasklet;
    private final BizTasklet3 bizTasklet3;
    private final BizTasklet4 bizTasklet4;
    private final CleanUpTasklet cleanUpTasklet;

    //Simulating Process
    //- download
    //- process file
    //- process another business items - businessTask3
    //- businessTask4
    //- cleanUp step

    @Bean
    public Job parallelFlowJob() {
        return jobs.get("parallel-flow-job")
                .incrementer(new RunIdIncrementer())
                .start(downloadStep())
                .next(fileProcessStep())
                .next(biz3Step())
                .next(biz4Step())
                .next(cleanUpStep())
                .build();
    }

    @Bean
    Step downloadStep() {
        return steps.get("downloadStep")
                .tasklet(downloadTasklet)
                .build();
    }

    @Bean
    Step fileProcessStep() {
        return steps.get("fileProcessStep")
                .tasklet(fileProcessTasklet)
                .build();
    }

    @Bean
    Step biz3Step() {
        return steps.get("biz3Step")
                .tasklet(bizTasklet3)
                .build();
    }

    @Bean
    Step biz4Step() {
        return steps.get("biz4Step")
                .tasklet(bizTasklet4)
                .build();
    }

    @Bean
    Step cleanUpStep() {
        return steps.get("cleanUpStep")
                .tasklet(cleanUpTasklet)
                .build();
    }

}
