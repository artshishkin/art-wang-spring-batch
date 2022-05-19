package net.shyshkin.study.itemwriters;

import net.shyshkin.study.itemwriters.config.AppConfiguration;
import net.shyshkin.study.itemwriters.config.CsvWriteBatchConfiguration;
import net.shyshkin.study.itemwriters.model.Product;
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

@ContextConfiguration(classes = {AppConfiguration.class, CsvWriteBatchConfiguration.class})
class CsvWriterJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/productOut.csv";

    @Autowired
    FlatFileItemWriter<Product> itemWriter;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("csvInputFile", "../input/product.csv");
        paramsBuilder.addString("csvOutputFile", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    void csvWriterJobTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("csvWriteJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(5, new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    void csvWriterStepTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("writeCsv", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(3));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(5, new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    void csvWriterStepScopeTest() throws Exception {
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