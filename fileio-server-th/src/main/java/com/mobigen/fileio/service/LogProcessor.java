package com.mobigen.fileio.service;

import com.mobigen.fileio.controller.KConsumer;
import com.mobigen.fileio.dto.FileLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogProcessor {
    Logger logger = LoggerFactory.getLogger(LogProcessor.class);

    @Autowired
    private LogParser logParser;
    @Autowired
    private DBProcessor dbProcessor;
    @Autowired
    private ErrorLogManager errorLogManager;

    @Async("executorLogProc")
    public void processLog(List<String> logBuffer) {
        boolean isSuccProcess = false;

        try{
            logger.info("---------- 처리 시작 : " + logBuffer.size() +" 개 ---------");
            long procStart = System.currentTimeMillis();

            // 파싱
            List<FileLog> parsedLogs = logParser.getParsedLogs(logBuffer);
            // DB작업
            if(parsedLogs.size() > 0){
                isSuccProcess = dbProcessor.insertLogs(parsedLogs);
            }

            long procEnd = System.currentTimeMillis();
            logger.info("총 처리 시간: " + (float)(procEnd - procStart)/1000 + "초");
            if(isSuccProcess){
                logger.info("------------ 처리 완료 -----------");

            } else {
                logger.info("------------ 처리 실패 -----------");

                // 실패한 경우 에러 로그 기록
                errorLogManager.writeErrorLog(new Exception("로그 파싱 및 DB작업 실패"), logBuffer);
            }

        } catch (Exception e){
            logger.error("로그 파싱 및 DB작업 실패", e);

            // 실패한 경우 에러 로그 기록
            errorLogManager.writeErrorLog(e, logBuffer);
        }
    }


}
