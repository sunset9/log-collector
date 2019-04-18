package com.mobigen.fileio.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;

@Configuration
@EnableAsync
public class LogProcConfig implements AsyncConfigurer {
    /** 기본 Thread 수 */
    private static int TASK_CORE_POOL_SIZE = 1;
    /** 최대 Thread 수 */
    private static int TASK_MAX_POOL_SIZE = 5;
    /** QUEUE 수 */
    private static int TASK_QUEUE_CAPACITY = 0;
    /** Thread */
    @Autowired
    private ThreadPoolTaskExecutor executorLogProc;

    @Override
    @Bean(name = "executorLogProc")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(TASK_CORE_POOL_SIZE);
        executor.setMaxPoolSize(TASK_MAX_POOL_SIZE);
        executor.setQueueCapacity(TASK_QUEUE_CAPACITY);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

    /**
     * Thread 등록 가능 여부
     *
     * @return
     */
    public boolean isTaskExecute() {
        boolean rtn = true;

        System.out.println("EXECUTOR_SAMPLE.getActiveCount() : " + executorLogProc.getActiveCount());

        // 실행중인 task 개수가 최대 개수(max + queue)보다 크거나 같으면 false
        if (executorLogProc.getActiveCount() >= (TASK_MAX_POOL_SIZE + TASK_QUEUE_CAPACITY)) {
            rtn = false;
        }

        return rtn;
    }

}
