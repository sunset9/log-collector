package com.mobigen.fileio.service;

import com.mobigen.fileio.dto.PosInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.*;

@Controller
public class PosFileManager {
    Logger logger = LoggerFactory.getLogger(PosFileManager.class);
    private File file;

    /**
     * 생성자
     *
     * @param dirPath
     * @param fileName
     */
    private PosFileManager(@Value("${pos.dirPath}") String dirPath, @Value("${pos.fileName}") String fileName){
        file = new File(dirPath + "/" + fileName);
    }

    /**
     * 로컬 파일에 시작, 종료 포인터 기록하는 메소드
     *
     * @param posInfo
     */
    public void writePos(PosInfo posInfo){
        FileWriter fw = null;
        try {
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            fw = new FileWriter(file);

            long start = posInfo.getStartPos();
            long end = posInfo.getEndPos();

            fw.write(start + "/" + end);
        } catch (Exception e) {
            logger.error("파일 쓰기 실패", e);
        } finally {
            if(fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 파일에 저장된 포인터 정보 가져오기
     *
     * @return
     */
    public PosInfo getStoredPos() {
        PosInfo posInfo = new PosInfo();

        if(file.exists()){
            try (BufferedReader br = new BufferedReader(new FileReader(file))){
                String line = br.readLine();

                if(line!= null){
                    posInfo.setStartPos(Long.parseLong(line.split("/")[0]));
                    posInfo.setEndPos(Long.parseLong(line.split("/")[1]));
                }
            } catch (Exception e) {
                logger.error("포인터 가져오기 실패", e);
            }
        } else {
            logger.error("포인터 정보 저장용 파일이 존재하지 않습니다.");
        }

        return posInfo;
    }

}
