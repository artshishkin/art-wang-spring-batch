package net.shyshkin.study.itemreaders;

import net.shyshkin.study.itemreaders.config.BatchConfiguration;
import net.shyshkin.study.itemreaders.config.FixReadBatchConfiguration;
import net.shyshkin.study.itemreaders.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ContextConfiguration(classes = {BatchConfiguration.class, FixReadBatchConfiguration.class})
class FixReaderJobTest extends AbstractJobTest {

    @Autowired
    FlatFileItemReader<Product> itemReader;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("fixInputFile", "../input/productFix.txt");
        return paramsBuilder.toJobParameters();
    }

    @Test
    void fixReaderJobText() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("fixReadJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void fixReaderStepTest() {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("readFix", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(3));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void fixReaderStepScopeTest() throws Exception {
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
