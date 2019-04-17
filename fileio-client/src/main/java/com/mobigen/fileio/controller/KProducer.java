package com.mobigen.fileio.controller;

import com.mobigen.fileio.service.ErrorLogManager;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Properties;

@Controller
public class KProducer {
    Logger logger = LoggerFactory.getLogger(KProducer.class);

    @Autowired
    ErrorLogManager errorLogManager;

    @Value("${kafka.bootstrapServer}")
    String BOOTSTRAP_SERVER;
    @Value("${kafka.topic}")
    String TOPIC;
    @Value("${kafka.client.id}")
    String CLIENT_ID;
    @Value("${kafka.acks}")
    String ACKS;

    private Producer<String, String> producer;

    /**
     * kafka로 메세지 전송 메소드
     *
     * @param filteredLog
     */
    public void send(List<String> filteredLog) {

        try{
            logger.info("보낼 로그:" + filteredLog.size() +"개");

            // Producer 객체 얻기
            producer = getProducer();

            // 전송
            for(String log : filteredLog ){
                ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, log);

                producer.send(record);
            }

            producer.flush();
            logger.info("전송 완료");

        } catch (Exception e) {
            logger.error("kafka 전송 실패", e);
            closeProducer();

            // 에러 로그 기록
            errorLogManager.writeErrorLog(e, filteredLog);
        }

    }

    /**
     * Producer 객체 가져오는 메소드
     *
     * @return
     */
    private Producer<String, String> getProducer(){
        Producer<String, String> producer;
        if(this.producer == null ){
            Properties prop = new Properties();
            prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
            prop.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
            prop.put(ProducerConfig.ACKS_CONFIG, ACKS);
            prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

            producer = new KafkaProducer<>(prop);
        } else {
            producer = this.producer;
        }

        return producer;

    }

    /**
     * Producer 객체 종료 메소드
     */
    private void closeProducer(){
        try{
            if(producer != null) {
                producer.close();
            }
        } catch (Exception e) {
            logger.error("Producer 닫기 실패", e);
        } finally {
            producer = null;
        }

    }
}
