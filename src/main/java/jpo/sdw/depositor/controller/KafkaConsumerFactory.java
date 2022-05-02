package jpo.sdw.depositor.controller;

import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import jpo.sdw.depositor.DepositorProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;

public class KafkaConsumerFactory {

   private KafkaConsumerFactory() {
      throw new UnsupportedOperationException();
   }

   public static KafkaConsumer<String, String> createConsumer(DepositorProperties depositorProperties) {
      Properties props = new Properties();
      props.put("bootstrap.servers", depositorProperties.getKafkaBrokers());

      props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
      props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 100000);
      props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);

      String kafkaType = getEnvironmentVariable("KAFKA_TYPE");
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

      String username = getEnvironmentVariable("CONFLUENT_KEY");
      String password = getEnvironmentVariable("CONFLUENT_SECRET");

      if (username != null && password != null) {
         String auth = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                 "username=\"" + username + "\" " +
                 "password=\"" + password + "\";";
         props.put("sasl.jaas.config", auth);
      }
   }

   private static String getEnvironmentVariable(String variableName) {
      String value = System.getenv(variableName);
      if (value == null || value.equals("")) {
         System.out.println("Something went wrong retrieving the environment variable " + variableName);
      }
      return value;
   }
}
