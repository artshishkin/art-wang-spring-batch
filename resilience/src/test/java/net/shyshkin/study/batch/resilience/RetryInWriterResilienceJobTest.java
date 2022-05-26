package net.shyshkin.study.batch.resilience;

import net.shyshkin.study.batch.resilience.config.AppConfiguration;
import net.shyshkin.study.batch.resilience.config.WriterRetryResilienceBatchConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ContextConfiguration(classes = {AppConfiguration.class, WriterRetryResilienceBatchConfiguration.class})
@ActiveProfiles("retry-in-writer")
class RetryInWriterResilienceJobTest extends AbstractJobTest {

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("writerRetryCsvInputFile", "../input/resilience/product-retryProc.csv");
        return paramsBuilder.toJobParameters();
    }

    @Test
    @DisplayName("When Writer throws RetryableWriterException then ProcessorAndWriter should retry")
    void writerRetryJobTest_whenException_shouldRetry() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("writerRetryJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("When Writer throws RetryableWriterException ProcessorAndWriter should retry in Step")
    void retryStepTest_whenException_shouldRetry() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("writerRetryStep", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertAll(
                        () -> assertThat(execution.getWriteCount()).isEqualTo(3),
                        () -> assertThat(execution.getReadCount()).isEqualTo(3),
                        () -> assertThat(execution.getReadSkipCount()).isEqualTo(0),
                        () -> assertThat(execution.getProcessSkipCount()).isEqualTo(0),
                        () -> assertThat(execution.getRollbackCount()).isEqualTo(2)
                ));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
    }
}