package org.zapply.product.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    private static final int POOL_SIZE = 3;
    private static final String THREAD_NAME_PREFIX = "My-scheduler-thread-";

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
        scheduler.initialize();
        return scheduler;
    }
}
