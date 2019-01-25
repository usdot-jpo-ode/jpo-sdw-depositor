package jpo.sdw.depositor.controller;

import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaConsumerFactory {

   private KafkaConsumerFactory() {
      throw new UnsupportedOperationException();
   }

   public static KafkaConsumer<String, String> createConsumer(String kafkaBrokers) {
      Properties props = new Properties();
      props.put("bootstrap.servers", kafkaBrokers);
      props.put("group.id", "test");
      props.put("enable.auto.commit", "true");
      props.put("auto.commit.interval.ms", "1000");
      props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

      return new KafkaConsumer<String, String>(props);
   }
}
