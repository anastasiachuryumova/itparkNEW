package newsaggregator.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import newsaggregator.model.sql.News;
import newsaggregator.repository.nosql.NewsAggregatorJpaRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.domain.Sort.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Lazy
@Configuration
@RequiredArgsConstructor
public class JobConfig {

    private static final int CHUNK_SIZE = 1;
    public static final String DB_MIGRATION_JOB = "dbMigrationJob";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final ItemReadListener<News> itemReadListener = new ItemReadListener<>() {

        @Override
        public void beforeRead() {
            log.info("Начало чтения");
        }

        @Override
        public void afterRead(News news) {
            log.info("Конец чтения");
        }

        @Override
        public void onReadError(Exception e) {
            log.info("Ошибка чтения");
        }
    };

    private final ItemWriteListener<newsaggregator.model.nosql.News> itemWriteListener = new ItemWriteListener<>() {

        @Override
        public void beforeWrite(List<? extends newsaggregator.model.nosql.News> list) {
            log.info("Начало записи");
        }

        @Override
        public void afterWrite(List<? extends newsaggregator.model.nosql.News> list) {
            log.info("Конец записи");
        }

        @Override
        public void onWriteError(Exception e, List<? extends newsaggregator.model.nosql.News> list) {
            log.info("Ошибка записи");
        }
    };

    private final ItemProcessListener<News, newsaggregator.model.nosql.News> itemProcessListener = new ItemProcessListener<>() {

        @Override
        public void beforeProcess(News o) {
            log.info("Начало обработки");
        }

        @Override
        public void afterProcess(News o, newsaggregator.model.nosql.News o2) {
            log.info("Конец обработки");
        }

        @Override
        public void onProcessError(News o, Exception e) {
            log.info("Ошибка обработки");
        }
    };

    private final ChunkListener chunkListener = new ChunkListener() {
        @Override
        public void beforeChunk(ChunkContext chunkContext) {
            log.info("Начало пачки");
        }

        @Override
        public void afterChunk(ChunkContext chunkContext) {
            log.info("Конец пачки");
        }

        @Override
        public void afterChunkError(ChunkContext chunkContext) {
            log.info("Ошибка пачки");
        }
    };

    private final JobExecutionListener jobExecutionListener = new JobExecutionListener() {
        @Override
        public void beforeJob(JobExecution jobExecution) {
            log.info("Начало job");
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            log.info("Конец job");
        }
    };

    @Lazy
    @Bean
    public JobBuilderFactory jobBuilderFactory() {
        return null;
    }

    @Bean
    public RepositoryItemReader<newsaggregator.model.sql.News> newsReader(NewsAggregatorJpaRepository newsRepository) {
        val newsRepositoryItemReader = new RepositoryItemReader<newsaggregator.model.sql.News>();
        newsRepositoryItemReader.setRepository(newsRepository);
        newsRepositoryItemReader.setMethodName("findAll");
        Map<String, Direction> sorts = new HashMap<>();
        sorts.put("id", Direction.ASC);
        newsRepositoryItemReader.setSort(sorts);
        return newsRepositoryItemReader;
    }

    @Bean
    public ItemProcessor<newsaggregator.model.sql.News, newsaggregator.model.nosql.News> newsProcessor () {
        return newsProcessor();
    }

    @Bean
    public RepositoryItemWriter<newsaggregator.model.nosql.News> newsWriter(NewsAggregatorJpaRepository newsRepository) {
        RepositoryItemWriter<newsaggregator.model.nosql.News> itemWriter = new RepositoryItemWriter<>();
        itemWriter.setRepository(newsRepository);
        itemWriter.setMethodName("save");
        return itemWriter;
    }

    @Bean
    public Step newsStep(RepositoryItemReader<newsaggregator.model.sql.News> newsReader,
                         ItemProcessor<newsaggregator.model.sql.News, newsaggregator.model.nosql.News> newsProcessor,
                         RepositoryItemWriter<newsaggregator.model.nosql.News> newsWriter) {
        return this.stepBuilderFactory.get("newsStep")
                .<newsaggregator.model.sql.News, newsaggregator.model.nosql.News>chunk(CHUNK_SIZE)
                .reader(newsReader)
                .processor(newsProcessor)
                .writer(newsWriter)
                .listener(itemReadListener)
                .listener(itemWriteListener)
                .listener(itemProcessListener)
                .listener(chunkListener)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step clearDbStep (Tasklet tasklet) {
        return this.stepBuilderFactory.get("clearDbStep")
                .tasklet(tasklet)
                .build();
    }

    @Bean
    public Tasklet tasklet(NewsAggregatorJpaRepository newsRepository) {
        return (contribution, chunkContext) -> {
            newsRepository.deleteAll();
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Job dbMigrationJob (
            Step clearDbStep
            , Step newsStep) {
        return jobBuilderFactory.get(DB_MIGRATION_JOB)
                .incrementer(new RunIdIncrementer())
                .start(clearDbStep)
                .next(newsStep)
                .listener(jobExecutionListener)
                .build();
    }
}
