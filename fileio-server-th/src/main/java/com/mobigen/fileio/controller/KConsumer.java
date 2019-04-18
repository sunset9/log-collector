package com.mobigen.fileio.controller;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Controller
public class KConsumer {
    Logger logger = LoggerFactory.getLogger(KConsumer.class);

    @Value("${kafka.bootstrap.servers}")
    String BOOTSTRAP_SERVER;
    @Value("${kafka.topic}")
    String TOPIC;
    @Value("${kafka.poll.duration.seconds}")
    long POLL_DURATION;
    @Value("${kafka.group.id}")
    String GROUP_ID;
    @Value("${kafka.enable.auto.commit}")
    String ENABLE_AUTO_COMMIT;
    @Value("${kafka.max.poll.records}")
    String MAX_POLL_RECORDS;
    @Value("${kafka.auto.offset.reset}")
    String AUTO_OFFSET_RESET;

    private Consumer<String, String> consumer;

    /**
     * kafka 통해서 로그 수집 메소드
     *
     * @return
     */
    public List<String> collectLog(){
        List<String> logs = new LinkedList<>();

        ConsumerRecords<String, String> consumerRecords;

        try{
            consumer = getConsumer();

            consumerRecords = consumer.poll(Duration.ofSeconds(POLL_DURATION));

            // 읽은 데이터 담아서 반환
            if(consumerRecords.count() > 0){
                for(ConsumerRecord<String,String> record: consumerRecords){
//                    logger.info("데이터 읽음: " + consumerRecords.count());
                    logs.add(record.value());
                }

                // 성공시 오프셋 commit
                consumer.commitSync();
            }

        } catch (Exception e){
            logger.error("로그 수집 실패", e);
            logs.clear();
            closeConsumer();
        }

        return logs;
    }

    /**
     * Consumer 객체 반환 메소드
     *
     * @return
     */
    private Consumer<String, String> getConsumer(){
        Consumer<String, String> consumer;

        if(this.consumer == null){
            Properties prop = new Properties();
            prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
            prop.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
            prop.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, ENABLE_AUTO_COMMIT);
            prop.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
            prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
            prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

            consumer = new KafkaConsumer<>(prop);
            consumer.subscribe(Collections.singletonList(TOPIC));

            logger.info("Kafka Consumer 생성");
        } else {
            consumer = this.consumer;
        }

        return consumer;
    }

    /**
     * Consumer 객체 종료 메소드
     */
    private void closeConsumer(){
        try{
            if(consumer != null) {
                consumer.close();
            }
        } catch (Exception e) {
            logger.error("Consumer 닫기 실패", e);
        } finally {
            consumer = null;
        }

    }
}
