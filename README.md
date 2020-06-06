# multiple event types demo

The purpose of this demo is to show how multiple event types can be stored in the same Avro serialized topic. The first step is to create Java classes from the Avro schemas:

    mvn generate-sources

There are two different record schemas:

1. name record (firstname, lastname)
2. address record (address, city, state, zip, country)

The producer uses the `TopicRecordNameStrategy`, which allows us to store different record types in the same topic.

The Avro consumer will deserialize the messages using the appropriate schema for each message:

    # kafka-avro-console-consumer --bootstrap-server cp01.woolford.io:9092 --topic multiple-event-types --property schema.registry.url="http://cp01.woolford.io:8081" --from-beginning
    {"firstname":"Alex","lastname":"Woolford"}
    {"address_1":"407 Arbor Drive","address_2":null,"city":"Lafayette","state":"CO","zip":"80026","country":"United States"}

At the time of writing (2020-06-06), the deserialized messages are displayed correctly in the Confluent Control Center, but the schemas themselves aren't displayed.

![c3 annotated screenshot](c3-multiple-event-types.png)