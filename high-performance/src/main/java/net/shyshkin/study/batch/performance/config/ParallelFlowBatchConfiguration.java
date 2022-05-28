package net.shyshkin.study.batch.performance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.performance.tasklet.DownloadTasklet;
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
                .build();
    }

    @Bean
    Step downloadStep() {
        return steps.get("downloadStep")
                .tasklet(new DownloadTasklet())
                .build();
    }

}
