package io.woolford;

import GetItemRecode.DataClassification;
import GetItemRecode.GetItemRecodeType;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MultiEventTypeProducer {

    private static Logger logger = LoggerFactory.getLogger(MultiEventTypeConsumer.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        // create and load default properties
        Properties props = new Properties();
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String propsPath = rootPath + "config.properties";
        FileInputStream in = new FileInputStream(propsPath);
        props.load(in);
        in.close();


        // create topic
        AdminClient adminClient = KafkaAdminClient.create(props);

        final String topicName = props.getProperty("multiple.event.types.topic");
        final short replicationFactor = 3;
        final int partitions = 1;

        if (!adminClient.listTopics().names().get().contains(topicName)){
            NewTopic multipleEventTypesTopic = new NewTopic(topicName, partitions, replicationFactor);
            final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(multipleEventTypesTopic));
            createTopicsResult.values().get(topicName).get();
        }

        // TODO: programatically change the schema compatibility mode

        // create Avro producer
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        KafkaProducer kafkaProducer = new KafkaProducer<>(props);

        // create an Avro name record
        NameRecord nameRecord = NameRecord.newBuilder()
                .setFirstname("Alex")
                .setLastname("Woolford")
                .build();

        // create an Avro address record
        AddressRecord addressRecord = AddressRecord.newBuilder()
                .setAddress1("407 Arbor Drive")
                .setAddress2(null)
                .setCity("Lafayette")
                .setState("CO")
                .setZip("80026")
                .setCountry("United States")
                .build();

        // send name record and address record to the same topic
        kafkaProducer.send(new ProducerRecord<Integer, NameRecord>(topicName, nameRecord));
        logger.info("NameRecord sent: " + nameRecord);

        kafkaProducer.send(new ProducerRecord<Integer, AddressRecord>(topicName, addressRecord));
        logger.info("AddressRecord sent: " + addressRecord);

        kafkaProducer.flush();
        logger.info("Kafka producer flushed.");

    }

}
