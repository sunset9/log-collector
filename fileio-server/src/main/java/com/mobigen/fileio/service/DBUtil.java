package com.mobigen.fileio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DBUtil {
    Logger logger = LoggerFactory.getLogger(DBUtil .class);

    @Value("${jdbc.connDurationTimeout}")
    int CONN_DURATION_TIMEOUT;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private Connection conn = null;
    private long latestConnUse = 0L;
    private boolean isUsing = false;

    /**
     * DB 연결된 후 일정 시간동안 아무 동작하지 않으면 연결 종료
     */
    public DBUtil(){
        Runnable checkConnTime = ()-> {
            try {
                if(conn != null && !conn.isClosed()
                        && !isUsing
                        && (float)(System.currentTimeMillis() - latestConnUse)/1000 >= CONN_DURATION_TIMEOUT ){
                    logger.info("DB 연결 시간이 오래 지나 강제 연결 종료");
                    close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        // 일정 주기로 동작하도록 등록
        service.scheduleAtFixedRate(checkConnTime, 5, 60, TimeUnit.SECONDS);
    }

    public Connection getConnection() throws Exception{
        Connection conn;
        try {
            if(this.conn == null || this.conn.isClosed()){
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                this.conn = conn;
                logger.info("DB Connection 객체 얻기");
            } else {
                conn = this.conn;
            }

            isUsing = true;

        } catch (Exception e) {
            logger.error("Connection 얻기 실패");
            throw e;
        }

        return conn;
    }

    public void commit(){
        try {
            conn.commit();
            latestConnUse = System.currentTimeMillis();
            isUsing = false;
        } catch (Exception e) {
            logger.error("Commit 실패", e);
            close();
        }
    }

    public void rollback(){
        try {
            conn.rollback();
            latestConnUse = System.currentTimeMillis();
            isUsing = false;
        } catch (Exception e) {
            logger.error("Rollback 실패" ,e);
            close();
        }
    }

    private void close(){
        try {
            conn.close();
            logger.info("DB Connection 연결 해제");
        } catch (Exception e) {
            logger.error("Connection Close 실패" ,e);
        }
    }

}
