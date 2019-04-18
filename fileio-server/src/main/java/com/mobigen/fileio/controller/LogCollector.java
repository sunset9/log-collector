package com.mobigen.fileio.controller;

import com.mobigen.fileio.dto.FileLog;
import com.mobigen.fileio.service.DBProcessor;
import com.mobigen.fileio.service.ErrorLogManager;
import com.mobigen.fileio.service.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.LinkedList;
import java.util.List;

@Controller
public class LogCollector {
    Logger logger = LoggerFactory.getLogger(KConsumer.class);

    @Autowired
    private KConsumer kafkaConsumer;
    @Autowired
    private LogParser logParser;
    @Autowired
    private DBProcessor dbProcessor;
    @Autowired
    private ErrorLogManager errorLogManager;

    @Value("${log.bufferSize}")
    private int buffSize;
    @Value("${log.maxTimeLimitSecond}")
    private int timeLimit;

    private static List<String> logBuffer;
    private static Long recentProcTime;


    public void start() {
        logger.info("로그 수집기 시작");

        logBuffer = new LinkedList<>();
        recentProcTime = System.currentTimeMillis();

        while(true){
            boolean isSuccProcess = true;

            try{
                // 카푸카 메세지 읽기
                List<String> readLogs = kafkaConsumer.collectLog();
                // 버퍼에 담기
                logBuffer.addAll(readLogs);

                // 버퍼가 차거나 일정 시간 지나면 버퍼 처리
                if(logBuffer.size() >= buffSize || (!isRecentProc() && logBuffer.size() > 0)){
                    isSuccProcess = false;

                    // 최근 작업 시간 기록
                    recentProcTime = System.currentTimeMillis();
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
                        // 초기화
                        logBuffer = new LinkedList<>();
                    } else { logger.info("------------ 처리 실패 -----------"); }
                }

            } catch (Exception e){
                logger.error("로그 수집 및 처리 실패", e);

                // 실패한 경우 에러 로그 기록
                errorLogManager.writeErrorLog(e, logBuffer);
                // 초기화
                logBuffer = new LinkedList<>();

            } finally {
                if(!isSuccProcess){
                    errorLogManager.writeErrorLog(new Exception("로그 처리 작업 실패"), logBuffer);
                    // 초기화
                    logBuffer = new LinkedList<>();
                }
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
