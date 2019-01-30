package jpo.sdw.depositor.consumerdepositors;

import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jpo.sdw.depositor.depositors.RestDepositor;

public class KafkaConsumerRestDepositor extends KafkaConsumerDepositor<String> {

   private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerRestDepositor.class);

   private RestDepositor<String> restDepositor;
   private KafkaConsumer<String, String> kafkaConsumer;

   public KafkaConsumerRestDepositor(KafkaConsumer<String, String> kafkaConsumer, RestDepositor<String> restDepositor) {
      this.setKafkaConsumer(kafkaConsumer);
      this.setRestDepositor(restDepositor);
   }

   @Override
   public void run(String topic) {
      this.kafkaConsumer.subscribe(Arrays.asList(topic));
      while (true) {
         ConsumerRecords<String, String> records = this.kafkaConsumer.poll(100);
         for (ConsumerRecord<String, String> record : records) {
            logger.info("offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
            logger.info("Depositing message to {}", this.getRestDepositor().getDestination().toString());
            this.getRestDepositor().deposit(record.value());
         }
      }
   }

   public RestDepositor<String> getRestDepositor() {
      return restDepositor;
   }

   public void setRestDepositor(RestDepositor<String> restDepositor) {
      this.restDepositor = restDepositor;
   }

   public KafkaConsumer<String, String> getKafkaConsumer() {
      return kafkaConsumer;
   }

   public void setKafkaConsumer(KafkaConsumer<String, String> kafkaConsumer) {
      this.kafkaConsumer = kafkaConsumer;
   }

}
