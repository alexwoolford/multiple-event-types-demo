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

        // create an Avro address record
        AddressRecord addressRecord = AddressRecord.newBuilder()
                .setAddress1("326 St Ida Circle")
                .setAddress2("")
                .setCity("Lafayette")
                .setState("Colorado")
                .setZip("80026")
                .setCountry("United States")
                .build();

        // calculate lengths for Avro and JSON (in bytes)
        int addressRecordAvroBytesLength = addressRecord.toByteBuffer().array().length;
        int addressRecordJsonBytesLength = addressRecord.toString().getBytes().length;

        logger.info(addressRecord.toString());
        logger.info("Avro bytes: " + addressRecordAvroBytesLength);
        logger.info("JSON bytes: " + addressRecordJsonBytesLength);

    }

}
