package jpo.sdw.depositor.consumerdepositors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.json.JSONObject;
import org.junit.Test;

import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor.LoopController;
import jpo.sdw.depositor.depositors.RestDepositor;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;

public class KafkaConsumerRestDepositorTest {

   @Tested
   KafkaConsumerRestDepositor testKafkaConsumerRestDepositor;

   @Injectable
   KafkaConsumer<String, String> injectableKafkaConsumer;
   @Injectable
   RestDepositor<String> injectableRestDepositor;
   @Injectable
   String encodeType;

   @Test
   public void runShouldDepositMessage() {

      List<ConsumerRecord<String, String>> crList = new ArrayList<ConsumerRecord<String, String>>();
      crList.add(new ConsumerRecord<String, String>("key", 0, 0, "value", "Message"));

      Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<TopicPartition, List<ConsumerRecord<String, String>>>();
      recordsMap.put(new TopicPartition("string", 0), crList);

      final ConsumerRecords<String, String> testConsumerRecords = new ConsumerRecords<String, String>(recordsMap);

      new MockUp<LoopController>() {
         @Mock
         public boolean loop(Invocation inv) {
            if (inv.getInvocationIndex() == 0) {
               return true;
            } else {
               return false;
            }
         }
      };

      new Expectations() {
         {
            injectableKafkaConsumer.poll((Duration)any);
            result = testConsumerRecords;

            injectableRestDepositor.deposit(anyString);
            times = 1;
         }
      };

      testKafkaConsumerRestDepositor.run("testTopic");
   }

    @Test
   public void runShouldDepositJSONMessage() {

      List<ConsumerRecord<String, String>> crList = new ArrayList<ConsumerRecord<String, String>>();
      crList.add(new ConsumerRecord<String, String>("key", 0, 0, "value", "{\"encodedMsg\":\"C4400000000680C0DE3\"}"));

      Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<TopicPartition, List<ConsumerRecord<String, String>>>();
      recordsMap.put(new TopicPartition("string", 0), crList);

      final ConsumerRecords<String, String> testConsumerRecords = new ConsumerRecords<String, String>(recordsMap);

      new MockUp<LoopController>() {
         @Mock
         public boolean loop(Invocation inv) {
            if (inv.getInvocationIndex() == 0) {
               return true;
            } else {
               return false;
            }
         }
      };

      new Expectations() {
         {
            injectableKafkaConsumer.poll((Duration)any);
            result = testConsumerRecords;

            injectableRestDepositor.deposit("{\"depositRequests\":[{\"encodeType\":\"\",\"encodedMsg\":\"C4400000000680C0DE3\"}]}");
            times = 1;
         }
      };

      testKafkaConsumerRestDepositor.run("testTopic");
   }

   @Test
   public void loopControllerShouldAlwaysReturnTrue() {
      assertTrue(LoopController.loop());
   }

   @Test
   public void testConstructorIsPrivate()
         throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      Constructor<LoopController> constructor = LoopController.class.getDeclaredConstructor();
      assertTrue(Modifier.isPrivate(constructor.getModifiers()));
      constructor.setAccessible(true);
      try {
         constructor.newInstance();
         fail("Expected IllegalAccessException.class");
      } catch (Exception e) {
         assertEquals(InvocationTargetException.class, e.getClass());
      }
   }

}