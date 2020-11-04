package io.woolford;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class EvolutionProducer {

    private static Logger logger = LoggerFactory.getLogger(EvolutionProducer.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        // create and load default properties
        Properties props = new Properties();
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String propsPath = rootPath + "config.properties";
        FileInputStream in = new FileInputStream(propsPath);
        props.load(in);
        in.close();


        // create topic
        // Note that the schema compatibility mode should be 'transitive forward'
        AdminClient adminClient = KafkaAdminClient.create(props);

        final String topicName = props.getProperty("person.topic");
        final short replicationFactor = 3;
        final int partitions = 1;

        if (!adminClient.listTopics().names().get().contains(topicName)){
            NewTopic personTopic = new NewTopic(topicName, partitions, replicationFactor);
            final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(personTopic));
            createTopicsResult.values().get(topicName).get();
        }

        // TODO: it's necessary to set the schema compatibility mode, e.g.
        //        http PUT cp01.woolford.io:8081/config/person-value/ <<< '
        //        {
        //            "compatibility": "FORWARD_TRANSITIVE"
        //        }'

        // create Avro producer
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        KafkaProducer kafkaProducer = new KafkaProducer<>(props);


        PersonRecord_v1 personRecord_v1 = new PersonRecord_v1();
        personRecord_v1.setLastName("Woolford");
        personRecord_v1.setFirstName("Alex");
        personRecord_v1.setAge(45);
        personRecord_v1.setGender(Gender.male);

        kafkaProducer.send(new ProducerRecord<Integer, PersonRecord_v1>(topicName, personRecord_v1));
        logger.info("PersonRecord_v1 sent: " + personRecord_v1);

        kafkaProducer.flush();
        logger.info("Kafka producer flushed.");


        // set breakpoint here, and change the schema compatibility to forward transitive
        PersonRecord_v2 personRecord_v2 = new PersonRecord_v2();
        personRecord_v2.setLastName("Mitnick");
        personRecord_v2.setFirstName("Kevin");
        personRecord_v2.setSsn("123-45-6789");
        personRecord_v2.setAge(57);
        personRecord_v2.setGender(Gender.male);

        kafkaProducer.send(new ProducerRecord<Integer, PersonRecord_v2>(topicName, personRecord_v2));
        logger.info("PersonRecord_v2 sent: " + personRecord_v2);

        kafkaProducer.flush();
        logger.info("Kafka producer flushed.");


        AddressRecord addressRecord = new AddressRecord();
        addressRecord.setAddress1("3250 O'Neal Circle");
        addressRecord.setAddress2("Unit E14");
        addressRecord.setCity("Boulder");
        addressRecord.setState("CO");
        addressRecord.setZip("80026");
        addressRecord.setCountry("United States");

        // put breakpoint below to show error
        kafkaProducer.send(new ProducerRecord<Integer, AddressRecord>(topicName, addressRecord));
        logger.info("Attempted to send AddressRecord: " + addressRecord);

        kafkaProducer.flush();
        logger.info("Kafka producer flushed.");

    }

}
