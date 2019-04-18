package com.mobigen.fileio.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Component
public class FileioDaoImpl implements FileioDao {
    Logger logger = LoggerFactory.getLogger(FileioDaoImpl.class);

    @Value("${insert.fetchSize}")
    int BATCH_SIZE;

    /**
     * 쿼리 수행 메소드
     *
     * @param conn
     * @param sqls
     * @return
     */
    @Override
    public Boolean executeQuery(Connection conn, List<String> sqls) {
        Boolean isSucc = false;

        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            stmt.setFetchSize(BATCH_SIZE);

            // 쿼리 실행
            for(String sql : sqls){
                if(Thread.currentThread().isInterrupted()) throw new Exception("interrupted");
                stmt.addBatch(String.valueOf(sql));
            }

            stmt.executeBatch();

            isSucc = true;
        } catch (Exception e){
            logger.error("DB 쿼리 수행 실패", e);
            isSucc = false;
        } finally {
            try {
                if(stmt!=null) stmt.close();
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }

        return isSucc;
    }


}
