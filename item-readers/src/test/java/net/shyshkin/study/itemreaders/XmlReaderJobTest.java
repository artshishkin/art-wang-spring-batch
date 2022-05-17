package net.shyshkin.study.itemreaders;

import net.shyshkin.study.itemreaders.config.BatchConfiguration;
import net.shyshkin.study.itemreaders.config.XmlReadBatchConfiguration;
import net.shyshkin.study.itemreaders.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ContextConfiguration(classes = {BatchConfiguration.class, XmlReadBatchConfiguration.class})
class XmlReaderJobTest extends AbstractJobTest {

    @Autowired
    StaxEventItemReader<Product> itemReader;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("xmlInputFile", "../input/product.xml");
        return paramsBuilder.toJobParameters();
    }

    @Test
    void xmlReaderJobText() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("xmlReadJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void xmlReaderStepTest() {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("readXml", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(3));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

    }

    @Test
    void xmlReaderStepScopeTest() throws Exception {
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
