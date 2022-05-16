package net.shyshkin.study.itemreaders;

import net.shyshkin.study.itemreaders.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBatchTest
@SpringBootTest
class CsvReaderJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    FlatFileItemReader<Product> itemReader;

    @AfterEach
    void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("inputFile", "../input/product.csv");
        return paramsBuilder.toJobParameters();
    }

    @Test
    void csvReaderJobText() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("csvReadJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void csvReaderStepTest() {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("readCsv", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(3));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void csvReaderStepScopeTest() throws Exception {
        //given
        StepExecution stepExecution = MetaDataInstanceFactory
                .createStepExecution(defaultJobParameters());

        //when
        StepScopeTestUtils.doInStepScope(stepExecution, () -> {
            Product productRead;
            itemReader.open(stepExecution.getExecutionContext());
            while ((productRead = itemReader.read()) != null) {

                //then
                Product product = productRead;
                assertAll(
                        () -> assertThat(product).hasNoNullFieldsOrProperties(),
                        () -> assertThat(product.getProductID()).isGreaterThan(0),
                        () -> assertThat(product.getPrice()).isGreaterThan(new BigDecimal("0.0")),
                        () -> assertThat(product.getProductName()).isNotEmpty(),
                        () -> assertThat(product.getProductDesc()).isNotEmpty()
                );
            }
            itemReader.close();
            return null;
        });
    }
}
