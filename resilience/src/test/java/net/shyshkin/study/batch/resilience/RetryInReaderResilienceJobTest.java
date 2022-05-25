package net.shyshkin.study.batch.resilience;

import net.shyshkin.study.batch.resilience.config.AppConfiguration;
import net.shyshkin.study.batch.resilience.config.RetryResilienceBatchConfiguration;
import net.shyshkin.study.batch.resilience.exception.Service500Exception;
import net.shyshkin.study.batch.resilience.model.Product;
import net.shyshkin.study.batch.resilience.service.ProductClient;
import net.shyshkin.study.batch.resilience.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.AssertFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.net.ConnectException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ContextConfiguration(classes = {AppConfiguration.class, RetryResilienceBatchConfiguration.class, ProductService.class})
@ActiveProfiles("retry-in-reader")
class RetryInReaderResilienceJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/resilience/retryProductOut.csv";

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
        paramsBuilder.addString("retryCsvOutputFile", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    @DisplayName("When External Service returns HttpStatus 500 Reader should retry")
    void retryJobTest_when500_shouldRetry() throws Exception {

        //given
        given(productClient.getProduct(anyLong()))
                .willThrow(Service500Exception.class)
                .willThrow(Service500Exception.class)
                .willReturn(defaultProduct)
                .willReturn(null);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("retryJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(3, new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    @DisplayName("When External Service returns HttpStatus 500 Reader should retry")
    void retryStepTest_when500_shouldRetry() throws Exception {
        //given
        given(productClient.getProduct(anyLong()))
                .willThrow(Service500Exception.class)
                .willThrow(Service500Exception.class)
                .willReturn(defaultProduct)
                .willReturn(null);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("retryStep", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertAll(
                        () -> assertThat(execution.getWriteCount()).isEqualTo(1),
                        () -> assertThat(execution.getReadCount()).isEqualTo(1),
                        () -> assertThat(execution.getReadSkipCount()).isEqualTo(0),
                        () -> assertThat(execution.getProcessSkipCount()).isEqualTo(0)
                ));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(3, new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    @DisplayName("When External Service Unreachable Reader should retry")
    void retryStepTest_whenConnectionException_shouldRetry() throws Exception {
        //given
        given(productClient.getProduct(anyLong()))
                .willThrow(ConnectException.class)
                .willThrow(ConnectException.class)
                .willReturn(defaultProduct)
                .willReturn(null);

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("retryStep", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertAll(
                        () -> assertThat(execution.getWriteCount()).isEqualTo(1),
                        () -> assertThat(execution.getReadCount()).isEqualTo(1),
                        () -> assertThat(execution.getReadSkipCount()).isEqualTo(0),
                        () -> assertThat(execution.getProcessSkipCount()).isEqualTo(0)
                ));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(3, new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    @DisplayName("When External Service Unreachable Reader should retry UNTIL EXCEED certain limit (5)")
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
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("retryStep", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertAll(
                        () -> assertThat(execution.getWriteCount()).isEqualTo(0),
                        () -> assertThat(execution.getReadCount()).isEqualTo(0),
                        () -> assertThat(execution.getReadSkipCount()).isEqualTo(0),
                        () -> assertThat(execution.getProcessSkipCount()).isEqualTo(0)
                ));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("FAILED");
        AssertFile.assertLineCount(2, new FileSystemResource(TEST_OUTPUT));
    }

}