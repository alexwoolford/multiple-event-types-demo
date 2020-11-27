package io.woolford;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AvroJsonSizeComparison {

    private static Logger logger = LoggerFactory.getLogger(AvroJsonSizeComparison.class);

    public static void main(String[] args) throws IOException {

        // create an Avro name record
        NameRecord nameRecord = NameRecord.newBuilder()
                .setFirstname("Alex")
                .setLastname("Woolford")
                .build();

        // calculate lengths for Avro and JSON (in bytes)
        int nameRecordAvroBytesLength = nameRecord.toByteBuffer().array().length;
        int nameRecordJsonBytesLength = nameRecord.toString().getBytes().length;

        logger.info(nameRecord.toString());
        logger.info("Avro bytes: " + nameRecordAvroBytesLength);
        logger.info("JSON bytes: " + nameRecordJsonBytesLength);

    }

}
