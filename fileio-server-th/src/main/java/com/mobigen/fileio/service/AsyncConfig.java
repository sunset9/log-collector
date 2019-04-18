//package com.mobigen.fileio.service;
//
//import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.AsyncConfigurer;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import javax.annotation.Resource;
//import java.util.concurrent.Executor;
//
//@Configuration
//@EnableAsync
//public class AsyncConfig implements AsyncConfigurer {
//    /** 샘플 기본 Thread 수 */
//    private static int TASK_SAMPLE_CORE_POOL_SIZE = 1;
//    /** 샘플 최대 Thread 수 */
//    private static int TASK_SAMPLE_MAX_POOL_SIZE = 5;
//    /** 샘플 QUEUE 수 */
//    private static int TASK_SAMPLE_QUEUE_CAPACITY = 0;
//    /** 샘플 Thread Bean Name */
//    private static String EXECUTOR_SAMPLE_BEAN_NAME = "executorSample";
//    /** 샘플 Thread */
//    @Resource(name = "executorSample")
//    private ThreadPoolTaskExecutor executorSample;
//
//
//    @Override
//    @Bean(name = "executorSample")
//    public ThreadPoolTaskExecutor getAsyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(TASK_SAMPLE_CORE_POOL_SIZE);
//        executor.setMaxPoolSize(TASK_SAMPLE_MAX_POOL_SIZE);
//        executor.setQueueCapacity(TASK_SAMPLE_QUEUE_CAPACITY);
//        executor.setBeanName(EXECUTOR_SAMPLE_BEAN_NAME);
//        executor.initialize();
//        return executor;
//
//    }
//
//    @Override
//    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
//        return null;
//    }
//
//    /**
//     * 샘플 Thread 등록 가능 여부
//     *
//     * @return 실행중인 task 개수가 최대 개수(max + queue)보다 크거나 같으면 false
//     */
//    public boolean isSampleTaskExecute() {
//        boolean rtn = true;
//
//        System.out.println("EXECUTOR_SAMPLE.getActiveCount() : " + executorSample.getActiveCount());
//
//        // 실행중인 task 개수가 최대 개수(max + queue)보다 크거나 같으면 false
//        if (executorSample.getActiveCount() >= (TASK_SAMPLE_MAX_POOL_SIZE + TASK_SAMPLE_QUEUE_CAPACITY)) {
//            rtn = false;
//        }
//
//        return rtn;
//    }
//
//    /**
//     * 샘플 Thread 등록 가능 여부
//     *
//     * @param createCnt : 생성 개수
//     * @return 실행중인 task 개수 + 실행할 개수가 최대 개수(max + queue)보다 크면 false
//     */
//    public boolean isSampleTaskExecute(int createCnt) {
//        boolean rtn = true;
//
//        // 실행중인 task 개수 + 실행할 개수가 최대 개수(max + queue)보다 크거나 같으면 false
//        if ((executorSample.getActiveCount() + createCnt) > (TASK_SAMPLE_MAX_POOL_SIZE + TASK_SAMPLE_QUEUE_CAPACITY)) {
//            rtn = false;
//        }
//
//        return rtn;
//    }
//
//}
