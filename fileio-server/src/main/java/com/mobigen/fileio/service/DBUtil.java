package com.mobigen.fileio.service;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.sql.Connection;

@Service
public class DBUtil {
    Logger logger = LoggerFactory.getLogger(DBUtil .class);

    @Autowired
    @Qualifier("dataSource")
    private ComboPooledDataSource dataSource;


    public Connection getConnection() throws Exception{
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } catch (Exception e) {
            logger.error("Connection 얻기 실패");
            throw e;
        }

        return conn;
    }

    public void commit(Connection conn){
        try {
            conn.commit();
        } catch (Exception e) {
            logger.error("Commit 실패", e);
        } finally {
            close(conn);
        }
    }

    public void rollback(Connection conn){
        try {
            conn.rollback();
        } catch (Exception e) {
            logger.error("Rollback 실패" ,e);
        } finally {
            close(conn);
        }
    }

    private void close(Connection conn){
        try {
            conn.close();
        } catch (Exception e) {
            logger.error("Connection Close 실패" ,e);
        }
    }

}
