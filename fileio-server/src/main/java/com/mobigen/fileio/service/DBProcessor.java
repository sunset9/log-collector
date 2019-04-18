package com.mobigen.fileio.service;

import com.mobigen.fileio.dao.FileioDaoImpl;
import com.mobigen.fileio.dto.FileLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class DBProcessor {
    Logger logger = LoggerFactory.getLogger(DBProcessor .class);

    @Value("${bulk.batchSize}")
    int BATCH_SIZE;

    @Autowired
    FileioDaoImpl fDao;

    /**
     * 쿼리문 생성 후 DB insert 작업 메소드
     *
     * @param parsedLogs
     * @return
     */
    public boolean insertLogs(List<FileLog> parsedLogs) {
        boolean isSucc = false;

        try{
            // 쿼리문 생성
            List<String> sqls = createSQL(parsedLogs);

            // DB 처리
            if(sqls.size() > 0){
                isSucc = fDao.executeQuery(sqls);
            }

        } catch (Exception e){
            logger.error("로그 버퍼 파싱 및 DB처리 실패", e);
            isSucc = false;
        }

        return isSucc;
    }

    /**
     * 완전한 쿼리문 생성
     *
     * @param parsedLogs
     * @return
     */
    private List<String> createSQL(List<FileLog> parsedLogs) {
        final String SUCC_SQL = "INSERT succ_filelog (date_ori, date_ts, hostname, sysname, content) VALUES";
        final String CDB_SQL = "INSERT cdb_filelog (date_ori, date_ts, hostname, sysname, content) VALUES";

        List<String> sqls = new LinkedList<>();
        StringBuffer succSqlValue = new StringBuffer();
        StringBuffer cdbSqlValue = new StringBuffer();

        int succLogCnt = 0;
        int cdbLogCnt = 0;

        try{
            // Bulk insert 문 생성
            for (FileLog log :parsedLogs) {
                // SUCCESS 로그 정보
                if ("SUCCESS".equals(log.getType())) {
                    succLogCnt++;
                    succSqlValue.append(log.toString()).append(",");

                    // 일정 BATCH_SIZE 단위로 저장
                    if (succLogCnt % BATCH_SIZE  == 0) {
                        // 완전한 쿼리문 생성
                        succSqlValue.setLength(succSqlValue.length() -1); // (),(),()
                        // 최종 리스트에 담기
                        sqls.add(SUCC_SQL + succSqlValue);
                        // 초기화
                        succSqlValue = new StringBuffer();
                    }

                // CDB 로그 정보
                } else if ("CDB".equals(log.getType())) {
                    cdbLogCnt++;
                    cdbSqlValue.append(log.toString()).append(",");

                    // 일정 BATCH_SIZE 단위로 저장
                    if (cdbLogCnt % BATCH_SIZE  == 0) {
                        // 완전한 쿼리문 생성
                        cdbSqlValue.setLength(cdbSqlValue.length() -1); // (),(),()
                        // 최종 리스트에 담기
                        sqls.add(CDB_SQL + cdbSqlValue);
                        // 초기화
                        cdbSqlValue = new StringBuffer();
                    }
                }
            }

            // 나머지
            if (succSqlValue.length() > 0){
                succSqlValue.setLength(succSqlValue.length() -1);
                sqls.add(SUCC_SQL + succSqlValue);
            }
            if (cdbSqlValue.length() > 0){
                cdbSqlValue.setLength(cdbSqlValue.length() -1);
                sqls.add(CDB_SQL + cdbSqlValue);
            }

            logger.info("쿼리 개수(SUCC): " + succLogCnt);
            logger.info("쿼리 개수(CDB): " + cdbLogCnt);


        } catch(Exception e){
            logger.error("쿼리문 생성 실패", e);
            sqls.clear();
        }

        return sqls;
    }

}

