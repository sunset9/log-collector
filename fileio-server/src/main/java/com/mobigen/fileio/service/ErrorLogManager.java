package com.mobigen.fileio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ErrorLogManager {
    Logger logger = LoggerFactory.getLogger(ErrorLogManager.class);

    /**
     * 에러로그 파일로 저장
     *
     * @param e 에러 메세지
     * @param errorLogs 작업 실패한 로그
     */
    public void writeErrorLog(Exception e, List<String> errorLogs){
        try{
            if(errorLogs != null && errorLogs.size() >0){
                logger.error("---------- 에러난 로그 개수:" + errorLogs.size() + "---------- ");
                logger.error(e.getMessage(), e);
                for(String log: errorLogs){
                    logger.error(log);
                }
            }
        } catch (Exception ec){
            logger.error("에러 로그 기록 실패", ec);
        }
    }
}
