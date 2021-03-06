package com.sample.autoscaling.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsync;
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Spring Context Configuration file
 */
@Configuration
@ComponentScan("com.sample.autoscaling")
@PropertySource("classpath:sample-application.properties")
@EnableScheduling
@EnableAsync
public class Config implements AsyncConfigurer, SchedulingConfigurer {

    @Value("${scheduler.thread.pool.size}")
    private int schedulerPoolSize;

    @Value("${executor.thread.pool.size}")
    private int executorPoolSize;

    @Value("${executor.max.pool.size}")
    private int executorMaxPoolSize;

    @Value("${executor.queue.capacity}")
    private int executorQueueCapacity;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.password}")
    private String redisPassword;

    @Value("${redis.port}")
    private int redisPort;

    /**
     * Bean to configure property placeholder.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        return new ClasspathPropertiesFileCredentialsProvider();
    }

    /**
     * Client Bean to interact with AutoScaling API (Async Version).
     */
    @Bean
    public AmazonAutoScalingAsync autoScalingAsyncClient() {
        return new AmazonAutoScalingAsyncClient(awsCredentialsProvider());
    }

    /**
     * Task Scheduler thread pool to schedule tasks which will be running in background.
     */
    @Bean(destroyMethod = "shutdown")
    public TaskScheduler getTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(schedulerPoolSize);
        taskScheduler.setThreadGroupName("Core Scheduler Thread Pool");
        taskScheduler.setThreadNamePrefix("Scheduler-");
        return taskScheduler;
    }

    /**
     * Set Task Scheduler for Scheduling jobs
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(getTaskScheduler());
    }

    /**
     * Set Async Executor for all the methods annotated with @Async annotation.
     */
    @Override
    @Bean(destroyMethod = "shutdown")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(executorPoolSize);
        executor.setMaxPoolSize(executorMaxPoolSize);
        executor.setQueueCapacity(executorQueueCapacity);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadGroupName("Core Executor Thread Pool");
        executor.setThreadNamePrefix("Executor-");
        return executor;
    }

    /**
     * Redis Connection Factory Bean
     */
    @Bean(destroyMethod = "destroy")
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
        redisConnectionFactory.setHostName(redisHost);
        redisConnectionFactory.setPassword(redisPassword);
        redisConnectionFactory.setPort(redisPort);
        return redisConnectionFactory;
    }

    /**
     * RedisTemplate that provides a high level abstraction for performing various Redis operations. It can be injected
     * like @Autowired private RedisTemplate<String, String> template; or can also be injected as ListOperations,
     * ValueOperations, SetOperations, ZSetOperations, and HashOperations (Property Editors are used to inject template
     * as Operations)
     */
    @Bean
    public RedisTemplate redisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory());
    }

    /**
     * Bean that will only interact with key terminate_instance_job_sequence in Redis to generate job sequence number.
     */
    @Bean
    public BoundValueOperations<String, Long> jobSequenceValueOps(
        @Value("${redis.job.sequence.key}")
        String jobSequenceKey) {
        return redisTemplate().boundValueOps(jobSequenceKey);
    }
}
