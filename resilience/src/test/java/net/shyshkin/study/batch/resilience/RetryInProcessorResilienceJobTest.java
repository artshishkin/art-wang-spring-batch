package net.shyshkin.study.batch.resilience;

import net.shyshkin.study.batch.resilience.config.AppConfiguration;
import net.shyshkin.study.batch.resilience.config.ProcRetryResilienceBatchConfiguration;
import net.shyshkin.study.batch.resilience.exception.Service500Exception;
import net.shyshkin.study.batch.resilience.model.Product;
import net.shyshkin.study.batch.resilience.service.ProductClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.AssertFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ContextConfiguration(classes = {AppConfiguration.class, ProcRetryResilienceBatchConfiguration.class})
class RetryInProcessorResilienceJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/resilience/product-retryProcOut.csv";

    @Autowired
    FlatFileItemWriter<Product> itemWriter;

    @MockBean
    ProductClient productClient;

    private Product defaultProduct = Product.builder()
            .productID(2L)
            .productName("Dell WEB")
            .productDesc("dell computer")
            .price(new BigDecimal("3000"))
            .unit(5L)
            .build();

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("procRetryCsvInputFile", "../input/resilience/product-retryProc.csv");
        paramsBuilder.addString("procRetryCsvOutputFile", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    @DisplayName("When External Service returns HttpStatus 500 Processor should retry")
    void procRetryJobTest_when500_shouldRetry() throws Exception {

        //given
        given(productClient.getProduct(anyLong()))
                .willThrow(Service500Exception.class)
                .willThrow(Service500Exception.class)
                .willReturn(defaultProduct)
                .willReturn(defaultProduct)
                .willReturn(defaultProduct);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("procRetryJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(5, new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    @DisplayName("When External Service returns HttpStatus 500 Processor should retry is Step")
    void retryStepTest_when500_shouldRetry() throws Exception {
        //given
        given(productClient.getProduct(anyLong()))
                .willThrow(Service500Exception.class)
                .willThrow(Service500Exception.class)
                .willReturn(defaultProduct)
                .willReturn(defaultProduct)
                .willThrow(Service500Exception.class)
                .willThrow(Service500Exception.class)
                .willReturn(defaultProduct);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("procRetryStep", defaultJobParameters());
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
                        () -> assertThat(execution.getRollbackCount()).isEqualTo(4)
                ));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(5, new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    @DisplayName("When External Service Unreachable Processor should retry")
    void retryStepTest_whenConnectionException_shouldRetry() throws Exception {
        //given
        given(productClient.getProduct(anyLong()))
                .willThrow(ConnectException.class)
                .willThrow(ConnectException.class)
                .willReturn(defaultProduct)
                .willReturn(defaultProduct)
                .willReturn(defaultProduct);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("procRetryStep", defaultJobParameters());
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
        AssertFile.assertLineCount(5, new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    @DisplayName("When External Service Unreachable Processor should retry UNTIL EXCEED certain limit (5)")
    void retryStepTest_whenConnectionException_shouldRetryUntilExceed() throws Exception {
        //given
        given(productClient.getProduct(anyLong()))
                .willThrow(ConnectException.class)
                .willThrow(ConnectException.class)
                .willThrow(ConnectException.class)
                .willThrow(ConnectException.class)
                .willThrow(ConnectException.class)
                .willThrow(ConnectException.class)
                .willThrow(ConnectException.class);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("procRetryStep", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertAll(
                        () -> assertThat(execution.getWriteCount()).isEqualTo(0),
                        () -> assertThat(execution.getReadCount()).isEqualTo(3),
                        () -> assertThat(execution.getReadSkipCount()).isEqualTo(0),
                        () -> assertThat(execution.getProcessSkipCount()).isEqualTo(0),
                        () -> assertThat(execution.getRollbackCount()).isEqualTo(6)
                ));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("FAILED");
        AssertFile.assertLineCount(2, new FileSystemResource(TEST_OUTPUT));
    }

}