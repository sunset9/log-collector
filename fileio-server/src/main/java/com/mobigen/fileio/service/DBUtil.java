package com.mobigen.fileio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class DBUtil {
    Logger logger = LoggerFactory.getLogger(DBUtil .class);

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private Connection conn = null;

    public DBUtil(){

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
        } catch (Exception e) {
            logger.error("Connection 얻기 실패");
            throw e;
        }

        return conn;
    }

    public void commit(){
        try {
            conn.commit();
        } catch (Exception e) {
            logger.error("Commit 실패", e);
            close();
        }
    }

    public void rollback(){
        try {
            conn.rollback();
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
