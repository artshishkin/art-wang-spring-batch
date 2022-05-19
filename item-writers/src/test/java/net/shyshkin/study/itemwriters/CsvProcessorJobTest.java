package net.shyshkin.study.itemwriters;

import net.shyshkin.study.itemwriters.config.AppConfiguration;
import net.shyshkin.study.itemwriters.config.CsvProcessorBatchConfiguration;
import net.shyshkin.study.itemwriters.model.Product;
import net.shyshkin.study.itemwriters.processor.ProductProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.test.AssertFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.FileCopyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {AppConfiguration.class, CsvProcessorBatchConfiguration.class, ProductProcessor.class})
class CsvProcessorJobTest extends AbstractJobTest {

    private static final String TEST_OUTPUT = "../output/productProcOut.csv";

    @Autowired
    FlatFileItemWriter<Product> itemWriter;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("csvInputFile", "../input/product.csv");
        paramsBuilder.addString("csvProcOutputFile", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    void csvProcJobTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("csvProcWriteJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(4, new FileSystemResource(TEST_OUTPUT));
        checkOutputFileContent();
    }

    @Test
    void csvProcStepTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("writeCsvProc", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(2));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");
        AssertFile.assertLineCount(4, new FileSystemResource(TEST_OUTPUT));
        checkOutputFileContent();
    }

    private void checkOutputFileContent() throws IOException {
        String outputContent = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(TEST_OUTPUT)));

        assertThat(outputContent)
                .contains("_A_P_P_L_E_")
                .contains("_O_F_F_I_C_E_")
                .contains("enohp llec elppa")
                .contains("erawtfos eciffo sm")
                .contains("The file was created "+ LocalDate.now())
                .doesNotContain("_D_E_L_L_");
    }
}