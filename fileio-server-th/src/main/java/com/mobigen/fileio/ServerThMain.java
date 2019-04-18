package com.mobigen.fileio;

import com.mobigen.fileio.controller.LogCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ServerThMain {
    @Autowired
    private LogCollector logCollector;

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/fileio-server-th_spring-config.xml");
        ServerThMain main = ctx.getBean(ServerThMain.class);

        main.start();
    }

    private void start(){
        Logger logger = LoggerFactory.getLogger(ServerThMain.class);
        try {
            logger.info("서버 실행");
            logCollector.start();

        } catch (Exception e) {
            logger.error("서버 에러", e);
        }
    }

}
