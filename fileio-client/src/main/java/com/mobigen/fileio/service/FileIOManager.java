package com.mobigen.fileio.service;

import com.mobigen.fileio.dto.PosInfo;
import com.mobigen.fileio.dto.ReadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.LinkedList;
import java.util.List;


@Service
public class FileIOManager {
    Logger logger = LoggerFactory.getLogger(FileIOManager.class);

    /**
     * RandomAccessFile 읽기용 객체 반환 메소드
     *
     * @param file
     * @return
     */
    private RandomAccessFile getRandomAccessFile(File file) {
        RandomAccessFile raf = null;

        boolean isSucc = false;
        int tryCnt = 1;

        while(!isSucc){
            try {
                raf = new RandomAccessFile(file, "r");
                isSucc = true;
            } catch (FileNotFoundException e) {
                logger.info("RandomAccessFile 객체 생성 실패, 재시도");
                tryCnt ++;

                if(tryCnt > 5){
                    break;
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e1) {
                    logger.error(e1.getMessage());
                }
            }
        }
        return raf;
    }

    /**
     * 원하는 포인트로부터 파일 읽는 메소드
     *
     * @param file 읽으려는 파일
     * @param curPosInfo 시작 포인터
     * @return
     */
    public ReadInfo readFile(File file, PosInfo curPosInfo) {

        ReadInfo readInfo = new ReadInfo();

        String line;
        List<String> readList = new LinkedList<>();
        BufferedReader br = null;

        try {
            if (!file.exists()) {
                logger.error("읽으려는 파일이 존재하지 않습니다. : " + file.getName());
            }
            // RandomAccessFile 객체 가져오기
            RandomAccessFile raf = getRandomAccessFile(file);

            // 읽으려는 시작포인터 설정
            raf.seek(curPosInfo.getEndPos());

            br = new BufferedReader(new InputStreamReader(new
                    FileInputStream(raf.getFD()), "UTF-8"));

            // 마지막 줄까지 읽기
            while ((line = br.readLine()) != null) {
                readList.add(new String(line.getBytes("ISO-8859-1"), "UTF-8"));
            }


            // 읽은 내용 저장
            readInfo.setReadLines(readList);

            // 시작 포인터 저장
            // 현재까지 읽은 포인터 저장
            PosInfo readPos = new PosInfo();
            readPos.setStartPos(curPosInfo.getEndPos());
            readPos.setEndPos(raf.getFilePointer());
            readInfo.setPosInfo(readPos);

        } catch (Exception e){
            logger.error("파일 읽기 실패", e);
            readList.clear();

            // 읽은 내용 초기화
            readInfo.setReadLines(readList);
            // 기존 포인터 저장
            readInfo.setPosInfo(curPosInfo);

        } finally {
            try {
                if(br!=null) br.close();
            } catch (Exception e) {
                logger.error("BufferedReader 객체 닫기 실패", e);
            }
        }

        return readInfo;
    }
}
