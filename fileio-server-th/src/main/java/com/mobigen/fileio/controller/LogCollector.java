package com.mobigen.fileio.controller;

import com.mobigen.fileio.config.LogProcConfig;
import com.mobigen.fileio.dto.FileLog;
import com.mobigen.fileio.service.DBProcessor;
import com.mobigen.fileio.service.ErrorLogManager;
import com.mobigen.fileio.service.LogParser;
import com.mobigen.fileio.service.LogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

@Controller
public class LogCollector {
    Logger logger = LoggerFactory.getLogger(KConsumer.class);

    @Autowired
    private KConsumer kafkaConsumer;
    @Autowired
    LogProcConfig logProcConfig;
    @Autowired
    LogProcessor logProcessor;
    @Autowired
    private ErrorLogManager errorLogManager;

    @Value("${log.bufferSize}")
    private int buffSize;
    @Value("${log.maxTimeLimitSecond}")
    private int timeLimit;

    private static List<String> logBuffer;
    private static Long recentProcTime = 0L;

    public void start() {
        logger.info("로그 수집기 시작");

        logBuffer = new LinkedList<>();

        while(true){

            try{
                // 카푸카 메세지 읽기
                List<String> readLogs = kafkaConsumer.collectLog();
                // 버퍼에 담기
                logBuffer.addAll(readLogs);

//                logger.info("현재 버퍼 사이즈: " + logBuffer.size());
                // 버퍼가 차거나 일정 시간 지나면 버퍼 처리
                if(logBuffer.size() >= buffSize || (!isRecentProc() && logBuffer.size() > 0)){

                    // 최근 작업 시간 기록
                    recentProcTime = System.currentTimeMillis();

                    // 스레드 등록 가능 여부 체크
                    if(logProcConfig.isTaskExecute()){
                        logProcessor.processLog(logBuffer);
                    } else {
                        logger.info("처리 스레드 등록 개수 초과");
                    }

                    // 초기화
                    logBuffer = new LinkedList<>();

                    logger.info("버퍼 처리 작업 완료");
                }

            } catch (Exception e){
                logger.error("로그 수집 실패", e);

                // 실패한 경우 에러 로그 기록
                errorLogManager.writeErrorLog(e, logBuffer);
                // 초기화
                logBuffer = new LinkedList<>();
            }
        }
    }

    /**
     * 일정 시간 내에 DB 작업 했는지 여부 반환 메소드
     *
     * @return 일정 시간내에 작업했으면 true 반환
     */
    private boolean isRecentProc() {
        long now = System.currentTimeMillis();

        if((now - recentProcTime)/1000 < timeLimit){
            return true;
        } else
            return false;
    }

}
