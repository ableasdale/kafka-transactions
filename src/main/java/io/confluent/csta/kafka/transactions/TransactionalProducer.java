package io.confluent.csta.kafka.transactions;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class TransactionalProducer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static void getCommittedEndOffset() {
        final Properties props = Config.getBaseConsumerProperties();

        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "latest");
        props.put("isolation.level","read_committed");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cg-1");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        try {
            consumer.subscribe(List.of( Config.TxnTopic));
            Set<TopicPartition> assignment;
            while ((assignment = consumer.assignment()).isEmpty()) {
                consumer.poll(Duration.ofMillis(10));
            }
            consumer.endOffsets(assignment).forEach((partition, offset) -> LOG.info("Read Committed Isolation: "+ partition + ": " + offset));
        } finally {
            LOG.info("********* CLOSING 'COMMITTED' CONSUMER *************");
            consumer.close();
        }
    }

    private static void getUncomittedCommittedEndOffset() {
        final Properties props = Config.getBaseConsumerProperties();

        props.put("enable.auto.commit", "false");
        props.put("auto.offset.reset", "latest");
        props.put("isolation.level","read_uncommitted");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cg-2");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        try {
            consumer.subscribe(List.of( Config.TxnTopic));
            Set<TopicPartition> assignment;
            while ((assignment = consumer.assignment()).isEmpty()) {
                consumer.poll(Duration.ofMillis(10));
            }
            consumer.endOffsets(assignment).forEach((partition, offset) -> LOG.info("Read Uncommitted Isolation: "+ partition + ": " + offset));
        } finally {
            LOG.info("********* CLOSING 'UNCOMMITTED' CONSUMER *************");
            consumer.close();
        }
    }

    public static void main(String[] args) {

        final Properties props = Config.getBaseProducerProperties();

        // Transaction specific properties
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "txn-1");
        props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "30000");

        Producer<String, String> producer = new KafkaProducer<>(props);

        producer.initTransactions();

        try {
            producer.beginTransaction();
            LOG.info("*** Begin Transaction ***");
            LOG.info(String.format("*** transactional.id %s ***", props.get("transactional.id")));

            for (int i = 0; i < 5; i++) {
                double randomDouble = Math.random();
                int randomNum = (int) (randomDouble * 1000000000);
                producer.send(new ProducerRecord<>(Config.TxnTopic, Integer.toString(i),
                        Integer.toString(randomNum)));
                LOG.info(String.format("Sent %d:%d", i, randomNum));
            }
            LOG.info("*** Before Committing Transaction ***");
            getUncomittedCommittedEndOffset();
            getCommittedEndOffset();
            LOG.info("*** Committing Transaction ***");
            producer.commitTransaction();
            getUncomittedCommittedEndOffset();
            getCommittedEndOffset();
            LOG.info("*** Committed Transaction ***");
        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            // DLQ?
            LOG.error(e.toString());
            producer.close();
        } catch (KafkaException e) {
            LOG.error(e.toString());
            producer.abortTransaction();
        }
        producer.flush();
        producer.close();
    }
}

