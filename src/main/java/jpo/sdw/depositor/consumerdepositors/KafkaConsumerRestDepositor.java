package jpo.sdw.depositor.consumerdepositors;

import java.time.Duration;
import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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
   private JSONObject jsonMsgList;
   private JSONObject jsonMsg;

   public KafkaConsumerRestDepositor(KafkaConsumer<String, String> kafkaConsumer, RestDepositor<String> restDepositor,
         String encodeType) {
      this.setKafkaConsumer(kafkaConsumer);
      this.setRestDepositor(restDepositor);
      this.jsonMsgList = new JSONObject();
      this.jsonMsg = new JSONObject();
      this.jsonMsg.put("EncodeType", encodeType);
   }

   @Override
   public void run(String... topics) {
      this.getKafkaConsumer().subscribe(Arrays.asList(topics));
      while (LoopController.loop()) { // NOSONAR (used for unit testing)
         ConsumerRecords<String, String> records = this.getKafkaConsumer().poll(Duration.ofMillis(100));
         JSONArray jsonRequests = new JSONArray();
         for (ConsumerRecord<String, String> record : records) {
            logger.info("Depositing message {}", record);
            this.jsonMsg.put("EncodedMsg", record.value());
            jsonRequests.put(jsonMsg);
         }
         if (records.count() != 0) {
            this.jsonMsgList.put("depositRequests", jsonRequests);
            this.getRestDepositor().deposit(jsonMsgList.toString());
         }
         try {
            // add 1 second sleep between sending messages to SDX
            TimeUnit.SECONDS.sleep(1);
         } catch (InterruptedException e) {
            logger.error("InterruptedException: {}", e.getMessage());
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
