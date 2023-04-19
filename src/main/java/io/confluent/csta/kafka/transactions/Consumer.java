package io.confluent.csta.kafka.transactions;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class Consumer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        final Properties props = Config.getBaseConsumerProperties();

        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "earliest");
        props.put("isolation.level","read_committed");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cg-1");

        try (var consumer = new KafkaConsumer<String, String>(props)) {
            consumer.subscribe(List.of( Config.TxnTopic));
            while (true) {
                ConsumerRecords<?, ?> records = consumer.poll(Duration.ofSeconds(5));
                for (ConsumerRecord<?, ?> record : records)
                    LOG.info(String.format("Partition: %s Offset: %s Value: %s Thread Id: %s", record.partition(), record.offset(), record.value(), Thread.currentThread().getId()));
            }
        }
    }
}
