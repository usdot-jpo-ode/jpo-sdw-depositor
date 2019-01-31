package jpo.sdw.depositor.consumerdepositors;

import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.depositors.RestDepositor;

public class KafkaConsumerRestDepositor extends KafkaConsumerDepositor<String> {

   @Autowired
   DepositorProperties depositorProperties;

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

   public KafkaConsumerRestDepositor(KafkaConsumer<String, String> kafkaConsumer, RestDepositor<String> restDepositor) {
      this.setKafkaConsumer(kafkaConsumer);
      this.setRestDepositor(restDepositor);
   }

   @Override
   public void run(String topic) {
      this.getKafkaConsumer().subscribe(Arrays.asList(topic));
      while (LoopController.loop()) { // NOSONAR (used for unit testing)
         ConsumerRecords<String, String> records = this.getKafkaConsumer().poll(100);
         for (ConsumerRecord<String, String> record : records) {

            long offset = record.offset();
            String key = record.key();
            String value = record.value();
            String destination = this.getRestDepositor().getDestination().toString();

            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("systemDepositName", "string");
            jsonMsg.put("encodeType", "hex");
            jsonMsg.put("encodedMsg", value);

            logger.info("Depositing message to {} KafkaOffset = {}, KafkaKey = {}, MessageValue = {}", destination,
                  offset, key, value);
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
