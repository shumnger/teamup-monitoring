package ru.team.up.teamup.demo;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.team.up.teamup.entity.Control;
import ru.team.up.teamup.entity.InitiatorType;
import ru.team.up.teamup.entity.Report;
import ru.team.up.teamup.entity.Status;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class Producer {
    public static void main(String[] args) throws InterruptedException {
        Logger logger = LoggerFactory.getLogger(Producer.class);
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        KafkaProducer<String, Report> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 100; i++) {
            Map<String, Object> param = new HashMap<>();
            param.put("count", i);
            param.put("isEnable", true);
            param.put("lastUpdate", new Date());

            InitiatorType initiatorType;
            int initiatorTypeCount = InitiatorType.values().length;
            initiatorType = switch (i % initiatorTypeCount) {
                case 1 -> InitiatorType.USER;
                case 2 -> InitiatorType.MANAGER;
                case 3 -> InitiatorType.ADMIN;
                default -> InitiatorType.SYSTEM;
            };

            Report report = new Report("" + i, Control.AUTO, initiatorType,"name_" +
                    initiatorType.name(), 100L + i, new Date(), Status.SUCCESS, param);

            ProducerRecord<String, Report> record = new ProducerRecord<>("input-data", initiatorType.name(),
                    report);

            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    logger.info("received new metadata, topic: " + metadata.topic() + " partition: " +
                            metadata.partition() + " offsets: " + metadata.offset() + " time: " +
                            metadata.timestamp());
                } else {
                    logger.error("error producing", exception);
                }
            });
            Thread.sleep(5000);
        }
        producer.flush();
        producer.close();
    }
}



