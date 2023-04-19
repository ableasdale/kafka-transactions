package io.confluent.csta.kafka.transactions;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.Random;

public class TransactionalProducer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        // Load producer configuration settings from a local file
        final Properties props = new Properties();
        props.put("bootstrap.servers", "https://localhost:9093");

        // This is the necessary configuration for configuring TLS/SSL on the Producer
        props.put("security.protocol", "SSL");
        props.put("ssl.truststore.location", "security/kafka.client.truststore.jks");
        props.put("ssl.truststore.password", "confluent");
        props.put("ssl.keystore.location", "security/kafka.client.keystore.jks");
        props.put("ssl.keystore.password", "confluent");

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        final String topic = "txn";

        String[] users = {"eabara", "jsmith", "sgarcia", "jbernard", "htanaka", "awalther"};
        String[] items = {"book", "alarm clock", "t-shirts", "gift card", "batteries"};
        Producer<String, String> producer = new KafkaProducer<>(props);

        final Long numMessages = 1000L;

        for (Long i = 0L; i < numMessages; i++) {
            Random rnd = new Random();
            String user = users[rnd.nextInt(users.length)];
            String item = items[rnd.nextInt(items.length)];

            producer.send(
                    new ProducerRecord<>(topic, user, item),
                    (event, ex) -> {
                        if (ex != null)
                            ex.printStackTrace();
                        else
                            LOG.info(String.format("Produced event to topic %s: key = %-10s value = %s", topic, user, item));
                    });
        }

        producer.flush();
        LOG.info(String.format("%s events were produced to topic %s%n", numMessages, topic));
        producer.close();

    }
}
