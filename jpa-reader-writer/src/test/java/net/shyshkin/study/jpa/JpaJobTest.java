package net.shyshkin.study.jpa;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.jpa.model.Category;
import net.shyshkin.study.jpa.model.Review;
import net.shyshkin.study.jpa.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@SqlGroup({
        @Sql(scripts = {"classpath:jdbc/products-schema.sql", "classpath:jdbc/products-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:jdbc/drop-product-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
class JpaJobTest extends AbstractJobTest {

    @Autowired
    JpaCursorItemReader<Category> itemReader;

    @Autowired
    DataSource dataSource;

    @Autowired
    ReviewRepository reviewRepository;

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        return paramsBuilder.toJobParameters();
    }

    @Test
    void jpaReaderJobTest() throws Exception {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualJobInstance.getJobName()).isEqualTo("jpaJob");
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(9);

    }

    @Test
    void jpaReaderStepTest() {

        //when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("readJpa", defaultJobParameters());
        var actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        //then
        assertThat(actualStepExecutions)
                .hasSize(1)
                .allSatisfy(execution -> assertThat(execution.getWriteCount()).isEqualTo(2));
        assertThat(actualJobExitStatus.getExitCode()).isEqualTo("COMPLETED");

        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(9);

    }

    @Test
    void jpaReaderStepScopeTest() throws Exception {
        //given
        StepExecution stepExecution = MetaDataInstanceFactory
                .createStepExecution(defaultJobParameters());

        //when
        StepScopeTestUtils.doInStepScope(stepExecution, () -> {
            Category categoryRead;
            boolean readSomeThing = false;
            itemReader.open(stepExecution.getExecutionContext());
            while ((categoryRead = itemReader.read()) != null) {

                //then
                Category category = categoryRead;
                assertAll(
                        () -> assertThat(category).hasNoNullFieldsOrProperties(),
                        () -> assertThat(category.getProducts())
                                .isNotNull()
                                .isNotEmpty()
                                .allSatisfy(product -> assertAll(
                                        () -> assertThat(product.getProductID()).isGreaterThan(0),
                                        () -> assertThat(product.getPrice()).isGreaterThan(new BigDecimal("0.0")),
                                        () -> assertThat(product.getProductName()).isNotEmpty(),
                                        () -> assertThat(product.getProductDesc()).isNotEmpty()
                                )),
                        () -> log.debug("{}", category)
                );
                readSomeThing = true;
            }
            itemReader.close();
            assertThat(readSomeThing).isTrue();
            return null;
        });
    }


}