package net.shyshkin.study.itemreaders;

import net.shyshkin.study.itemreaders.config.BatchConfiguration;
import net.shyshkin.study.itemreaders.config.JsonReadBatchConfiguration;
import net.shyshkin.study.itemreaders.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ContextConfiguration(classes = {BatchConfiguration.class, JsonReadBatchConfiguration.class})
class JsonReaderJobTest extends AbstractJobTest {

    @Autowired
    JsonItemReader<Product> itemReader;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("jsonInputFile", "../input/product.json");
        return paramsBuilder.toJobParameters();
    }

    @Test
    void jsonReaderJobText() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("jsonReadJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void jsonReaderStepTest() {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("readJson", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(3));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void jsonReaderStepScopeTest() throws Exception {
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
