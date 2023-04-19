package io.confluent.csta.kafka.transactions;

import java.util.Properties;

public class Config {

    protected static String TxnTopic = "transaction-topic";
    protected static String BOOTSTRAP_SERVER = "https://localhost:9093";

    protected static Properties getBaseProducerProperties(){
        final Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVER);
        putTLSProperties(props);

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    private static void putTLSProperties(Properties props) {
        // This is the necessary configuration for configuring TLS/SSL on the Producer
        props.put("security.protocol", "SSL");
        props.put("ssl.truststore.location", "security/kafka.client.truststore.jks");
        props.put("ssl.truststore.password", "confluent");
        props.put("ssl.keystore.location", "security/kafka.client.keystore.jks");
        props.put("ssl.keystore.password", "confluent");
    }

    protected static Properties getBaseConsumerProperties(){
        final Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVER);
        putTLSProperties(props);

        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }
}
