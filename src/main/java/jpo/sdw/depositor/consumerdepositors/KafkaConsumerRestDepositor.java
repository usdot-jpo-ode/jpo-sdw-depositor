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
   private String encodeType;

   public KafkaConsumerRestDepositor(KafkaConsumer<String, String> kafkaConsumer, RestDepositor<String> restDepositor,
         String encodeType) {
      this.setKafkaConsumer(kafkaConsumer);
      this.setRestDepositor(restDepositor);
      this.jsonMsgList = new JSONObject();
      this.encodeType = encodeType;
   }

   @Override
   public void run(String... topics) {
      this.getKafkaConsumer().subscribe(Arrays.asList(topics));
      while (LoopController.loop()) { // NOSONAR (used for unit testing)
         ConsumerRecords<String, String> records = this.getKafkaConsumer().poll(Duration.ofMillis(100));
         JSONArray jsonRequests = new JSONArray();

         for (ConsumerRecord<String, String> record : records) {
            logger.info("Depositing message {}", record);

            JSONObject depositMessage = prepareJSONObject(record.value());

            jsonRequests.put(new JSONObject(depositMessage.toString()));
         }
         if (records.count() != 0) {
            this.jsonMsgList.put("depositRequests", jsonRequests);
            this.getRestDepositor().deposit(jsonMsgList.toString());
         }
      }
   }

   private JSONObject prepareJSONObject(String record) {
      // Old version treated record as value for static field "encodedMsg"
      // Try/Catch around new optional json object to attach meta data. Fall back if
      // record isn't json.
      JSONObject deposit = new JSONObject();
      deposit.put("encodeType", this.encodeType);
      try {
         JSONObject recordObj = new JSONObject(record);
         deposit.put("encodedMsg", recordObj.getString("encodedMsg"));
         deposit.put("estimatedRemovalDate", recordObj.opt("estimatedRemovalDate"));
      } catch (Exception e) {
         deposit.put("encodedMsg", record);
      }
      return deposit;      
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
