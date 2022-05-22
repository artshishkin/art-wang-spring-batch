package net.shyshkin.study.batch.resilience;

import net.shyshkin.study.batch.resilience.config.AppConfiguration;
import net.shyshkin.study.batch.resilience.config.SkipResilienceBatchConfiguration;
import net.shyshkin.study.batch.resilience.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {AppConfiguration.class, SkipResilienceBatchConfiguration.class})
class SkipResilienceJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/resilience/productOut.csv";

    @Autowired
    FlatFileItemWriter<Product> itemWriter;

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
        AssertFile.assertLineCount(14, new FileSystemResource(TEST_OUTPUT));
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
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(3));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(14, new FileSystemResource(TEST_OUTPUT));
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