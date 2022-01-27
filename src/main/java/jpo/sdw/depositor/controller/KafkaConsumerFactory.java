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

      props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

      String kafkaType = System.getenv("KAFKA_TYPE");
      if (kafkaType != null && kafkaType.equals("CONFLUENT")) {
         addConfluentProperties(props);
      }

      props.put("group.id", depositorProperties.getGroupId());
      props.put("enable.auto.commit", "true");
      props.put("auto.commit.interval.ms", "1000");

      return new KafkaConsumer<>(props);
   }

   private static void addConfluentProperties(Properties props) {
      props.put("ssl.endpoint.identification.algorithm", "https");
      props.put("security.protocol", "SASL_SSL");
      props.put("sasl.mechanism", "PLAIN");

      String username = System.getenv("CONFLUENT_KEY");
      String password = System.getenv("CONFLUENT_SECRET");

      if (username != null && password != null) {
         String auth = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                 "username=\"" + username + "\" " +
                 "password=\"" + password + "\";";
         props.put("sasl.jaas.config", auth);
      }
      else {
         // logger.error("Environment variables CONFLUENT_KEY and CONFLUENT_SECRET are not set. Set these in the .env file to use Confluent Cloud");
         // TODO: log error
      }

   }
}
