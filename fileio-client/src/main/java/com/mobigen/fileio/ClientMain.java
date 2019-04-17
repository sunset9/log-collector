package com.mobigen.fileio;

import com.mobigen.fileio.controller.LogCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ClientMain {
    @Autowired
    LogCollector logCollector;

    Logger logger = LoggerFactory.getLogger(ClientMain.class);

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/fileio-client_spring-config.xml");
        ClientMain main = ctx.getBean(ClientMain.class);

        main.start();
    }

    private void start(){

        logger.info("시작");
        Long start = System.currentTimeMillis();

        logCollector.start();

        Long end = System.currentTimeMillis();
        logger.info("총 소요시간 : " + (end-start)/1000 + "초");
    }

}
