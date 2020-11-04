package io.woolford;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

public class MultiEventTypeConsumer {

    private static Logger logger = LoggerFactory.getLogger(MultiEventTypeConsumer.class);

    public static void main(String[] args) throws IOException {

        // create and load default properties
        Properties props = new Properties();
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String propsPath = rootPath + "config.properties";
        FileInputStream in = new FileInputStream(propsPath);
        props.load(in);
        in.close();

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        KafkaConsumer<String, GenericRecord> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(props.getProperty("multiple.event.types.topic")));
        while (true) {
            ConsumerRecords<String, GenericRecord> records = consumer.poll(100);
            for (ConsumerRecord<String, GenericRecord> record : records) {
                String key = record.key();

                // Generic record will handle different schema types
                GenericRecord value = record.value();

                logger.info(value.toString());

                if (value.getClass().getName().equals("io.woolford.NameRecord")){
                    handleNameRecord((NameRecord) value);
                }
            }
        }

    }

    private static void handleNameRecord(NameRecord nameRecord){
        logger.info("nameRecord handler");
    }


}
