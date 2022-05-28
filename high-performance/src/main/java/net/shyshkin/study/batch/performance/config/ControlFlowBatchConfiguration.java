package net.shyshkin.study.batch.performance.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.batch.performance.tasklet.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Slf4j
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
@Profile("control-flow")
public class ControlFlowBatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    private final DownloadTasklet downloadTasklet;
    private final FileProcessTasklet fileProcessTasklet;
    private final BizTasklet3 bizTasklet3;
    private final BizTasklet4Failed bizTasklet4Failed;
    private final CleanUpTasklet cleanUpTasklet;

    //Simulating Process
    //- download
    //- process file
    //- process another business items - businessTask3
    //- businessTask4
    //- cleanUp step

    @Bean
    public Job parallelFlowJob() {
        return jobs.get("control-flow-job")
                .incrementer(new RunIdIncrementer())
                .start(splitFlow())
                .next(cleanUpStep())
                .end()
                .build();
    }

    private Flow splitFlow() {
        return new FlowBuilder<SimpleFlow>("split-flow")
                .split(new SimpleAsyncTaskExecutor("split"))
                .add(fileFlow(), biz3Flow(), biz4Flow())
                .build();
    }

    private Flow fileFlow() {
        return new FlowBuilder<SimpleFlow>("file-flow")
                .start(downloadStep())
                .next(fileProcessStep())
                .build();
    }

    private Flow biz3Flow() {
        return new FlowBuilder<SimpleFlow>("business-3-flow")
                .start(biz3Step())
                .build();
    }

    private Flow biz4Flow() {
        return new FlowBuilder<SimpleFlow>("business-4-flow")
                .start(biz4Step())
                .on("FAILED")
                .to(pagerDutyStep())
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
                .tasklet(bizTasklet4Failed)
                .build();
    }

    @Bean
    Step cleanUpStep() {
        return steps.get("cleanUpStep")
                .tasklet(cleanUpTasklet)
                .build();
    }

    @Bean
    Step pagerDutyStep() {
        return steps.get("pager-duty")
                .tasklet(new PagerDutyTasklet())
                .build();
    }

}
