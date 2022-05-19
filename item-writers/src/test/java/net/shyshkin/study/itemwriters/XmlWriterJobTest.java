package net.shyshkin.study.itemwriters;

import net.shyshkin.study.itemwriters.config.AppConfiguration;
import net.shyshkin.study.itemwriters.config.XmlWriteBatchConfiguration;
import net.shyshkin.study.itemwriters.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileCopyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {AppConfiguration.class, XmlWriteBatchConfiguration.class})
class XmlWriterJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/productOut.xml";

    @Autowired
    StaxEventItemWriter<Product> itemWriter;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("csvInputFile", "../input/product.csv");
        paramsBuilder.addString("xmlOutputFile", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    void xmlWriterJobTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("xmlWriteJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        checkOutputFileContent();
    }

    @Test
    void xmlWriterStepTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("writeXml", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(3));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        checkOutputFileContent();
    }

    @Test
    void xmlWriterStepScopeTest() throws Exception {
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
        checkOutputFileContent();
    }

    private List<Product> mockProducts() {
        return List.of(
                Product.builder()
                        .productID(1L)
                        .productName("Apple")
                        .productDesc("apple cell phone")
                        .price(new BigDecimal("500.00"))
                        .unit(10L)
                        .build(),
                Product.builder()
                        .productID(2L)
                        .productName("Dell")
                        .productDesc("dell computer")
                        .price(new BigDecimal("3000"))
                        .unit(5L)
                        .build(),
                Product.builder()
                        .productID(3L)
                        .productName("office")
                        .productDesc("ms office software")
                        .price(new BigDecimal("196"))
                        .unit(23L)
                        .build()
        );
    }

    private void checkOutputFileContent() throws IOException {
        String outputContent = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(TEST_OUTPUT)));

        assertThat(outputContent)
                .contains("<products>")
                .contains("<product>")
                .contains("<prodId>")
                .contains("<prodName>Apple</prodName>")
                .contains("<prodDescription>apple cell phone</prodDescription>")
                .contains("<unit>23</unit>");
    }

}