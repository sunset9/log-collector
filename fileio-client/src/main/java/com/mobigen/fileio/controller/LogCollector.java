package com.mobigen.fileio.controller;

import com.mobigen.fileio.service.ErrorLogManager;
import com.mobigen.fileio.service.FileIOManager;
import com.mobigen.fileio.service.PosFileManager;
import com.mobigen.fileio.dto.PosInfo;
import com.mobigen.fileio.dto.ReadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@Controller
public class LogCollector {
    Logger logger = LoggerFactory.getLogger(LogCollector.class);

    @Autowired
    private FileIOManager fileIOManager;
    @Autowired
    private KProducer kafkaProducer;
    @Autowired
    private PosFileManager posFileManager;
    @Autowired
    private ErrorLogManager errorLogManager;
    
    @Value("${file.dirPath}")
    private String dirPath;
    @Value("${file.fileName}")
    private String fileName;
    @Value("${oldfile.fileName}")
    private String oldfileName;


    public void start() {
        try {
            File targetFile = new File(dirPath + "/" + fileName);

            // ------------ 기존파일 내용 처리 --------------
            proccessOldContent(targetFile);

            // ------------ 새로 추가된 내용 처리 --------------
            watchNewContent(targetFile);

        } catch (Exception e){
            logger.error("LogCollector service Error", e);
        }
    }

    /**
     * 기존에 작성된 내용 처리 메소드
     *
     * @param targetFile
     * @return
     */
    private void proccessOldContent(File targetFile){

        try{
            // 파일 존재 검사
            if(!targetFile.exists()){
                logger.error("작업할 파일이 존재하지 않습니다.");
                return ;
            }

            PosInfo posInfo = posFileManager.getStoredPos();

            // 파일 변경 여부 검사
            if(posInfo.getEndPos() > targetFile.length()){
                logger.info("파일이 변경되어 새로운 파일을 읽습니다.");
                posInfo.setStartPos(0);
                posInfo.setEndPos(0);
            }

            // 파일 읽기
            logger.info("(기존 파일) 읽기 전:" + posInfo.getEndPos());
            ReadInfo readInfo = fileIOManager.readFile(targetFile, posInfo);
            logger.info("(기존 파일) 읽은 후:" + readInfo.getPosInfo().getEndPos());

            if (readInfo.getReadLines().size() == 0) logger.info("기존파일: 읽을 내용 없음");

            // 현재 읽은 포인터 정보 파일로 저장
            posFileManager.writePos(readInfo.getPosInfo());

            // 서버로 전송
            List<String> filteredLog = filterLog(readInfo.getReadLines());
            if (filteredLog.size() > 0) kafkaProducer.send(filteredLog);


        } catch (Exception e){
            logger.error("기존 파일 처리 실패", e);
        }

    }

    /**
     * 새로운 파일 수정 감시 메소드
     *
     * @param targetFile
     */
    private void watchNewContent(File targetFile){
        PosInfo curPosInfo = null;
        WatchService watchService = null;
        WatchKey watchKey;
        List<WatchEvent<?>> events;
        ReadInfo readInfo = null;

        try {
            logger.info("Watch 서비스 시작");

            watchService = FileSystems.getDefault().newWatchService(); 
            // 파일 수정만 감시
            FileSystems.getDefault().getPath(dirPath).register(watchService, ENTRY_MODIFY);
            // 최근 포인터 정보 불러오기
            curPosInfo = posFileManager.getStoredPos();

            // 주기적으로 포인터 정보 파일로 저장하는 스케쥴러 동작
            scheduledWritePos(curPosInfo);

            while (true) {
                watchKey = watchService.take(); // 이벤트 오길 기다림
                events = watchKey.pollEvents(); //이벤트들을 가져옴

                for (WatchEvent<?> event : events) {
                    Path changed = (Path) event.context();

                    // 지정한 파일 변화 감지
                    if (changed.endsWith(fileName)) {
                        logger.info("-----------파일 수정 감지---------------");

                        // 파일 수정 완료 대기
                        while(!isFileCreateDone(dirPath)){
                            logger.info("파일 수정 완료될 때까지 대기중");
                            Thread.sleep(500);
                        }

                        long changedFileSize = targetFile.length();
                        logger.info("타겟 파일 크기: " + changedFileSize);

                        // ---- 읽으려는 파일의 사이즈 비교 ----
                        // 이전보다 큰 사이즈 (내용추가)
                        if(changedFileSize > curPosInfo.getEndPos()){
                            logger.info("읽기 전:" + curPosInfo.getEndPos());

                            // 파일 읽기
                            readInfo = fileIOManager.readFile(targetFile, curPosInfo);
                            // 현재 읽은 포인터 정보 업데이트
                            curPosInfo.setStartPos(readInfo.getPosInfo().getStartPos());
                            curPosInfo.setEndPos(readInfo.getPosInfo().getEndPos());

                            logger.info("읽은 후:" + curPosInfo.getEndPos());

                        // 작은 사이즈 (롤링)
                        } else if(changedFileSize < curPosInfo.getEndPos()){
                            // 이전 파일
                            File oldFile = new File(dirPath+'/'+ oldfileName);

                            logger.info("(이전 파일) 읽기 전:" + curPosInfo.getEndPos());

                            // 이전 파일 마저 끝까지 읽고
                            ReadInfo oldReadInfo = fileIOManager.readFile(oldFile, curPosInfo);
                            // 현재 읽은 포인터 정보 업데이트
                            curPosInfo.setStartPos(oldReadInfo.getPosInfo().getStartPos());
                            curPosInfo.setEndPos(oldReadInfo.getPosInfo().getEndPos());

                            logger.info("(이전 파일) 읽은 후: " + curPosInfo.getEndPos());

                            // 새 파일 처음부터 읽기
                            curPosInfo.setStartPos(0);
                            curPosInfo.setEndPos(0);
                            readInfo = fileIOManager.readFile(targetFile, curPosInfo);
                            // 현재 읽은 포인터 정보 업데이트
                            curPosInfo.setStartPos(readInfo.getPosInfo().getStartPos());
                            curPosInfo.setEndPos(readInfo.getPosInfo().getEndPos());

                            // 이전 파일과 새로운 파일 읽은 내용 합치기
                            List<String> combinedLines = new LinkedList<>();
                            combinedLines.addAll(oldReadInfo.getReadLines());
                            combinedLines.addAll(readInfo.getReadLines());
                            readInfo.setReadLines(combinedLines);

                            logger.info("(새로운 파일)읽은 후:" + curPosInfo.getEndPos());

                        // 같은 사이즈 - 넘어가기
                        } else {
                            continue;
                        }

                        // 파싱하여 서버로 전송
                        List<String> filteredLog = filterLog(readInfo.getReadLines());
                        if(filteredLog.size() > 0) kafkaProducer.send(filteredLog);
                    }
                }
                watchKey.reset();
            }

        } catch (Exception e) {
            // 현재 포인터 정보 파일로 저장해놓기
            posFileManager.writePos(curPosInfo);

            logger.error("파일 감시 실패", e);

            // 에러 로그 기록
            errorLogManager.writeErrorLog(e, readInfo.getReadLines());
            
            logger.info("10초 후 재실행");
            try {
                Thread.sleep(10 * 1000);
                watchNewContent(targetFile);
            } catch (InterruptedException e1) {
                logger.error("재실행 실패", e1);
            }

        } finally {
            try {
                if(watchService!= null) watchService.close();
            } catch (Exception e) {
                logger.error("watch 종료 실패", e);
            }
        }
    }

    /**
     * 파일 수정 감지 시 수정 완료 확인 메소드
     *
     * @param dirPath
     * @return
     */
    private boolean isFileCreateDone(String dirPath) {
        boolean isDone = false;

        try{
            File targetDir = new File(dirPath);
            if(targetDir.exists() && targetDir.isDirectory()){
                File[] bakFiles = targetDir.listFiles((dir, name) -> name.endsWith(".lck"));

                if(bakFiles !=null && bakFiles.length == 0){
                    isDone = true;
                }
            }
        } catch (Exception e){
            isDone = false;
            logger.error("lck 파일 검색 실패", e);
        }

        return isDone;

    }

    /**
     * 주기적으로 포인터 정보 저장 메소드
     *
     * @param posInfo
     */
    private void scheduledWritePos(final PosInfo posInfo) {
        Runnable writePosInfo = () -> {
            posFileManager.writePos(posInfo);
//                logger.info("posInfo 저장");
        };

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        // 일정 주기로 동작하도록 등록
        service.scheduleAtFixedRate(writePosInfo, 60, 30, TimeUnit.SECONDS);
    }


    /**
     * 서버로 보낼 로그 필터링 메소드
     *
     * @param logs
     * @return
     */
    private List<String> filterLog(List<String> logs){

        List<String> filteredLog = new ArrayList<>();

        try {
            logger.info("필터링 시작");

            if(logs != null && logs.size() > 0){
                /*
                final Pattern pattern = Pattern.compile("^.*(SUCCESS|CDB).*$");
                long start = System.currentTimeMillis();
                for(String log:logs){
                    if(pattern.matcher(log).find()){
                        filteredLog.add(log);
                    }
                }
                logger.info("필터링 소요 시간: " + (System.currentTimeMillis() - start) + "ms");
                */

                long start2 = System.currentTimeMillis();
                for (String log : logs) {
                    if (log.contains("SUCCESS") || log.contains("CDB")) {
                        filteredLog.add(log);
                    }
                }

                logger.info("필터링 소요 시간: " + (System.currentTimeMillis() - start2) + "ms");
            }

        } catch (Exception e){
            filteredLog.clear();

            logger.error("필터링 에러", e);
            errorLogManager.writeErrorLog(e, logs);
        }

        return filteredLog;
    }

}
