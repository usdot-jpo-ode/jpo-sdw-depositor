package jpo.sdw.depositor.consumerdepositors;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor.LoopController;
import jpo.sdw.depositor.depositors.RestDepositor;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class KafkaConsumerRestDepositorTest {

   @Tested
   KafkaConsumerRestDepositor testKafkaConsumerRestDepositor;

   @Injectable
   KafkaConsumer<String, String> injectableKafkaConsumer;
   @Injectable
   RestDepositor<String> injectableRestDepositor;

   @Mocked
   ConsumerRecord<String, String> mockConsumerRecord;

   @Test(timeout = 4000) // 4 second timeout for safety; this test overrides an infinite loop
   public void runShouldDepositMessage(@Capturing LoopController capturingLoopController) {

      List<ConsumerRecord<String, String>> crList = new ArrayList<ConsumerRecord<String, String>>();
      crList.add(new ConsumerRecord<String, String>("key", 0, 0, "value", null));

      Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<TopicPartition, List<ConsumerRecord<String, String>>>();
      recordsMap.put(new TopicPartition("string", 0), crList);

      final ConsumerRecords<String, String> testConsumerRecords = new ConsumerRecords<String, String>(recordsMap);

      new Expectations() {
         {
            LoopController.loop();
            returns(true, false);

            injectableKafkaConsumer.poll(anyLong);
            result = testConsumerRecords;

            injectableRestDepositor.deposit(anyString);
            times = 1;
         }
      };

      testKafkaConsumerRestDepositor.run("testTopic");
   }

   @SuppressWarnings("static-access") // for sonar class coverage
   @Test
   public void loopControllerShouldAlwaysReturnTrue() {
      assertTrue(new LoopController().loop());
   }

}
