package jpo.sdw.depositor.controller;

import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import jpo.sdw.depositor.DepositorProperties;

public class KafkaConsumerFactory {

   private KafkaConsumerFactory() {
      throw new UnsupportedOperationException();
   }

   public static KafkaConsumer<String, String> createConsumer(DepositorProperties depositorProperties) {
      Properties props = new Properties();
      props.put("bootstrap.servers", depositorProperties.getKafkaBrokers());
      props.put("group.id", depositorProperties.getGroupId());
      props.put("enable.auto.commit", "true");
      props.put("auto.commit.interval.ms", "1000");
      props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

      return new KafkaConsumer<String, String>(props);
   }
}
