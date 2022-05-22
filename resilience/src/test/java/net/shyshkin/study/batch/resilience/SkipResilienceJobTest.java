package net.shyshkin.study.batch.resilience;

import net.shyshkin.study.batch.resilience.config.AppConfiguration;
import net.shyshkin.study.batch.resilience.config.SkipResilienceBatchConfiguration;
import net.shyshkin.study.batch.resilience.listener.ProductSkipListener;
import net.shyshkin.study.batch.resilience.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ContextConfiguration(classes = {AppConfiguration.class, SkipResilienceBatchConfiguration.class, ProductSkipListener.class})
@TestPropertySource(properties = {"app.error.skip.file=../output/resilience/error_skipped_test.txt"})
class SkipResilienceJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/resilience/productOut.csv";
    public static final String ERROR_SKIPPED_FILE = "../output/resilience/error_skipped_test.txt";

    @Autowired
    FlatFileItemWriter<Product> itemWriter;

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(Path.of(ERROR_SKIPPED_FILE));
    }

    @AfterEach
    void cleanFile() throws IOException {
        Files.deleteIfExists(Path.of(ERROR_SKIPPED_FILE));
    }

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("csvInputFile", "../input/resilience/product-skip.csv");
        paramsBuilder.addString("csvOutputFile", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    void skipJobTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("skipJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(11, new FileSystemResource(TEST_OUTPUT));
        checkErrorFile();
    }

    @Test
    void skipStepTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("skipStep", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertAll(
                        () -> assertThat(execution.getWriteCount()).isEqualTo(9),
                        () -> assertThat(execution.getReadCount()).isEqualTo(9),
                        () -> assertThat(execution.getReadSkipCount()).isEqualTo(3)
                ));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(11, new FileSystemResource(TEST_OUTPUT));
        checkErrorFile();
    }

    @Test
    void skipStepScopeTest() throws Exception {
        //given
        StepExecution stepExecution = MetaDataInstanceFactory
                .createStepExecution(defaultJobParameters());

        //when
        StepScopeTestUtils.doInStepScope(stepExecution, () -> {
            itemWriter.open(stepExecution.getExecutionContext());
            itemWriter.write(mockProducts());
            itemWriter.close();
            return null;
        });

        //then
        AssertFile.assertLineCount(5, new FileSystemResource(TEST_OUTPUT));
    }

    private void checkErrorFile() throws IOException {
        Path errFile = Path.of(ERROR_SKIPPED_FILE);
        assertThat(errFile).exists();
        assertThat(Files.readAllLines(errFile))
                .hasSize(3)
                .allSatisfy(content -> assertThat(content)
                        .endsWith("error")
                );
    }

    private List<Product> mockProducts() {
        return List.of(
                Product.builder()
                        .productID(1L)
                        .productName("Apple WEB")
                        .productDesc("apple cell phone")
                        .price(new BigDecimal("500.00"))
                        .unit(10L)
                        .build(),
                Product.builder()
                        .productID(2L)
                        .productName("Dell WEB")
                        .productDesc("dell computer")
                        .price(new BigDecimal("3000"))
                        .unit(5L)
                        .build(),
                Product.builder()
                        .productID(3L)
                        .productName("office WEB")
                        .productDesc("ms office software")
                        .price(new BigDecimal("196"))
                        .unit(23L)
                        .build()
        );
    }
}