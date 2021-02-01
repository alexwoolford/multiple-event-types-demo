package io.woolford;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.IntStream;

public class AvroJsonSizeComparison {

    private static Logger logger = LoggerFactory.getLogger(AvroJsonSizeComparison.class);

    public static void main(String[] args) throws IOException {

        // create an Avro name record
        NameRecord nameRecord = AvroJsonSizeComparison.getNameRecord();

        // calculate lengths for Avro and JSON (in bytes)
        int nameRecordAvroBytesLength = nameRecord.toByteBuffer().array().length;
        int nameRecordJsonBytesLength = nameRecord.toString().getBytes().length;

        logger.info(nameRecord.toString());
        logger.info("Avro bytes: " + nameRecordAvroBytesLength);
        logger.info("JSON bytes: " + nameRecordJsonBytesLength);

        // create an Avro address record
        AddressRecord addressRecord = AvroJsonSizeComparison.getAddressRecord();

        // calculate lengths for Avro and JSON (in bytes)
        int addressRecordAvroBytesLength = addressRecord.toByteBuffer().array().length;
        int addressRecordJsonBytesLength = addressRecord.toString().getBytes().length;

        logger.info(addressRecord.toString());
        logger.info("Avro bytes: " + addressRecordAvroBytesLength);
        logger.info("JSON bytes: " + addressRecordJsonBytesLength);

        produceAvroAddressCompressed();

    }

    private static NameRecord getNameRecord(){
        // create an Avro name record
        return NameRecord.newBuilder()
                .setFirstname("Alex")
                .setLastname("Woolford")
                .build();
    }

    private static AddressRecord getAddressRecord(){
        // create an Avro address record
        return AddressRecord.newBuilder()
                .setAddress1("326 St Ida Circle")
                .setAddress2("")
                .setCity("Lafayette")
                .setState("Colorado")
                .setZip("80026")
                .setCountry("United States")
                .build();
    }


    private static Properties getProperties() throws IOException {
        // create and load default properties
        Properties props = new Properties();
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String propsPath = rootPath + "config.properties";
        FileInputStream in = new FileInputStream(propsPath);
        props.load(in);
        in.close();

        return props;
    }

    private static void produceAvroAddressCompressed() throws IOException {

        // This is the beginnings of a compression analysis. There are a couple of very important short-comings:
        //      1) we send 1M identical records. That's not a true test. We meed to generate data that's representative
        //         of real-life.
        //      2) compression is a trade-off between size and compute to compress/decompress. The compute aspect of
        //         this is completely ignored. The time to compress n records is also ignored.
        //      3) the topics should be created using the admin API to ensure that they're setup with the right settings.
        //         As it stands, this is a manual step.
        //      4) you should probably just use zstd, always (written Feb, 2021).

        Properties props = getProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("client.id", "avro-address-compressed");
//        props.put("compression.type", "none");
//        props.put("compression.type", "gzip");
        props.put("compression.type", "snappy");
//        props.put("compression.type", "lz4");
//        props.put("compression.type", "zstd");

        // create uncompressed Avro producer
        KafkaProducer kafkaProducerCompressed = new KafkaProducer<>(props);

        IntStream.range(1,1000000).forEach(i ->
                        kafkaProducerCompressed.send(
                                new ProducerRecord<Integer, AddressRecord>("compression-address-snappy", getAddressRecord()),
                                new Callback() {
                                    @Override
                                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                                        logger.info("Address compressed size: " + metadata.serializedValueSize());
                                    }
                                }
                        )
        );

        kafkaProducerCompressed.flush();

    }

}
