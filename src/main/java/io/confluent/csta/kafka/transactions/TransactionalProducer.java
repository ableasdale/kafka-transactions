package io.confluent.csta.kafka.transactions;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class TransactionalProducer {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
            LOG.info("*** Committing Transaction ***");
            producer.commitTransaction();
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

