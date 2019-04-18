package com.mobigen.fileio.dao;


import com.mobigen.fileio.service.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Component
public class FileioDaoImpl implements FileioDao {
    Logger logger = LoggerFactory.getLogger(FileioDaoImpl.class);

    @Autowired
    DBUtil dbUtil;
    @Value("${insert.fetchSize}")
    int BATCH_SIZE;
    @Value("${insert.timeoutSecond}")
    int TIMEOUT;

    /**
     * 쿼리 수행 메소드
     *
     * @param sqls
     * @return
     */
    @Override
    public Boolean executeQuery(List<String> sqls) {
        Boolean isSucc = false;

        Statement stmt = null;

        long start = System.currentTimeMillis();
        try {
            logger.info("DB 작업 시작");
            Connection conn = dbUtil.getConnection();

            stmt = conn.createStatement();
            stmt.setFetchSize(BATCH_SIZE);
            stmt.setQueryTimeout(TIMEOUT);

            // 쿼리 실행
            for(String sql : sqls){
                stmt.addBatch(String.valueOf(sql));
            }

            stmt.executeBatch();

            dbUtil.commit();
            isSucc = true;
        } catch (Exception e){
            logger.error("DB 쿼리 수행 실패", e);
            dbUtil.rollback();
            isSucc = false;
        } finally {
            try {
                if(stmt!=null) stmt.close();
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }

        long end = System.currentTimeMillis();
        logger.info("DB 작업 시간: " + (float)(end - start)/1000 + "초");

        return isSucc;
    }


}
