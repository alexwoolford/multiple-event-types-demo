package io.woolford;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // create topic
        final Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.KAFKA_BROKERS);
        config.put(AdminClientConfig.CLIENT_ID_CONFIG, KafkaConstants.CLIENT_ID);
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000);

        AdminClient adminClient = KafkaAdminClient.create(config);

        final String topicName = KafkaConstants.TOPIC;
        final short replicationFactor = 3;
        final int partitions = 1;

        if (!adminClient.listTopics().names().get().contains(topicName)){
            NewTopic multipleEventTypesTopic = new NewTopic(topicName, partitions, replicationFactor);
            final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(multipleEventTypesTopic));
            createTopicsResult.values().get(topicName).get();
        }

        // create Avro producer
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.KAFKA_BROKERS);
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConstants.CLIENT_ID);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        producerProps.put("value.subject.name.strategy", TopicRecordNameStrategy.class.getName());
        producerProps.put("schema.registry.url", KafkaConstants.SCHEMA_REGISTRY_URL);
        KafkaProducer kafkaProducer = new KafkaProducer<>(producerProps);

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
        kafkaProducer.send(new ProducerRecord<Integer, NameRecord>(KafkaConstants.TOPIC, nameRecord));
        kafkaProducer.send(new ProducerRecord<Integer, AddressRecord>(KafkaConstants.TOPIC, addressRecord));
        kafkaProducer.flush();

        System.out.println("OK");

    }

}
