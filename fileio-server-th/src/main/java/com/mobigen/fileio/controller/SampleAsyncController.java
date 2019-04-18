//package com.mobigen.fileio.controller;
//
//import com.mobigen.fileio.ServerMain;
//import com.mobigen.fileio.service.AsyncConfig;
//import com.mobigen.fileio.service.AsyncTaskSample;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Controller;
//
//import javax.annotation.Resource;
//
//@Controller("SampleAsyncController")
//public class SampleAsyncController {
//    Logger logger = LoggerFactory.getLogger(SampleAsyncController.class);
//    /**
//     * 샘플 스레드
//     */
//    @Resource(name = "asyncTaskSample")
//    private AsyncTaskSample asyncTaskSample;
//
//    /**
//     * AsyncConfig
//     */
//    @Resource(name = "asyncConfig")
//    private AsyncConfig asyncConfig;
//
//    public void ss() {
//
//        //////////////////////////////////////////////////////////////////////////////////////////////////
//        // 스레드 생성
//        //////////////////////////////////////////////////////////////////////////////////////////////////
//        try {
//            // 등록 가능 여부 체크
//            if (asyncConfig.isSampleTaskExecute()) {
//                // task 사용
//                asyncTaskSample.executorSample("ㄱ");
//            } else {
//                System.out.println("==============>>>>>>>>>>>> THREAD 개수 초과");
//            }
//        } catch (Exception e) {
//            // TaskRejectedException : 개수 초과시 발생
//            System.out.println("==============>>>>>>>>>>>> THREAD ERROR");
//            System.out.println("TaskRejectedException : 등록 개수 초과");
//            System.out.println("==============>>>>>>>>>>>> THREAD END");
//        }
//
//    }
//}
//
