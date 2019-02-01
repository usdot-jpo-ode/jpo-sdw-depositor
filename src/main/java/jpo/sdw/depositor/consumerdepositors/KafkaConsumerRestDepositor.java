package jpo.sdw.depositor.consumerdepositors;

import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jpo.sdw.depositor.depositors.RestDepositor;

public class KafkaConsumerRestDepositor extends KafkaConsumerDepositor<String> {

   public static class LoopController {
      private LoopController() {
         throw new UnsupportedOperationException();
      }

      public static boolean loop() {
         return true;
      }
   }

   private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerRestDepositor.class);

   private RestDepositor<String> restDepositor;
   private KafkaConsumer<String, String> kafkaConsumer;
   private JSONObject jsonMsg;

   public KafkaConsumerRestDepositor(KafkaConsumer<String, String> kafkaConsumer, RestDepositor<String> restDepositor,
         String encodeType) {
      this.setKafkaConsumer(kafkaConsumer);
      this.setRestDepositor(restDepositor);
      this.jsonMsg = new JSONObject();
      this.jsonMsg.put("systemDepositName", "SDW 2.3");
      this.jsonMsg.put("encodeType", encodeType);
   }

   @Override
   public void run(String... topics) {
      this.getKafkaConsumer().subscribe(Arrays.asList(topics));
      while (LoopController.loop()) { // NOSONAR (used for unit testing)
         ConsumerRecords<String, String> records = this.getKafkaConsumer().poll(100);
         for (ConsumerRecord<String, String> record : records) {
            logger.debug("Publishing message {}", record);
            this.jsonMsg.put("encodedMsg", record.value());
            this.getRestDepositor().deposit(jsonMsg.toString());
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
