//package com.mobigen.fileio.service;
//
//import com.mobigen.fileio.controller.SampleAsyncController;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//@Service("asyncTaskSample")
//public class AsyncTaskSample {
//    Logger logger = LoggerFactory.getLogger(AsyncTaskSample.class);
//    /**
//     * 시뮬레이션 테스트용 함수
//     *
//     * @param str
//     */
//    @Async("executorSample")
//    public void executorSample(String str) {
//        // LOG : 시작 입력
//        // ...
//        logger.info("==============>>>>>>>>>>>> THREAD START");
//
//        // 내용
//        // 내용
//        // 내용
//
//        // LOG : 종료 입력
//        // ...
//        logger.info("==============>>>>>>>>>>>> THREAD END");
//    }
//
//    /**
//     * 시뮬레이션 테스트용 함수2
//     *
//     * @param str
//     */
//    @Async("executorSample")
//    public void executorSample2(String str) {
//        // LOG : 시작 입력
//        // ...
//        System.out.println("==============>>>>>>>>>>>> THREAD START");
//
//        // 내용
//        // 내용
//        // 내용
//
//        // LOG : 종료 입력
//        // ...
//        System.out.println("==============>>>>>>>>>>>> THREAD END");
//    }
//
//
//}
