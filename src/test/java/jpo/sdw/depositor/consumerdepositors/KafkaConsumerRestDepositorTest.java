package jpo.sdw.depositor.consumerdepositors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor.LoopController;
import jpo.sdw.depositor.depositors.RestDepositor;

@RunWith(MockitoJUnitRunner.class)
public class KafkaConsumerRestDepositorTest {

   @Mock
   KafkaConsumer<String, String> injectableKafkaConsumer;

   @Mock
   RestDepositor<String> injectableRestDepositor;

   KafkaConsumerRestDepositor testKafkaConsumerRestDepositor;

   @Before
   public void before() {
      testKafkaConsumerRestDepositor = new KafkaConsumerRestDepositor(
            injectableKafkaConsumer, injectableRestDepositor, "");
   }

   @Test
   public void runShouldDepositMessage() {
      List<ConsumerRecord<String, String>> crList = new ArrayList<>();
      crList.add(new ConsumerRecord<>("key", 0, 0, "value", "Message"));

      Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<>();
      recordsMap.put(new TopicPartition("string", 0), crList);

      final ConsumerRecords<String, String> testConsumerRecords = new ConsumerRecords<>(recordsMap);

      when(injectableKafkaConsumer.poll(any(Duration.class))).thenReturn(testConsumerRecords);

      try (MockedStatic<LoopController> mockedLoop = mockStatic(LoopController.class)) {
         mockedLoop.when(LoopController::loop).thenReturn(true, false);

         testKafkaConsumerRestDepositor.run("testTopic");
      }

      verify(injectableRestDepositor, times(1)).deposit(anyString());
   }

   @Test
   public void runShouldDepositJSONMessage() {
      List<ConsumerRecord<String, String>> crList = new ArrayList<>();
      crList.add(new ConsumerRecord<>("key", 0, 0, "value", "{\"encodedMsg\":\"C4400000000680C0DE3\"}"));

      Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<>();
      recordsMap.put(new TopicPartition("string", 0), crList);

      final ConsumerRecords<String, String> testConsumerRecords = new ConsumerRecords<>(recordsMap);

      when(injectableKafkaConsumer.poll(any(Duration.class))).thenReturn(testConsumerRecords);

      try (MockedStatic<LoopController> mockedLoop = mockStatic(LoopController.class)) {
         mockedLoop.when(LoopController::loop).thenReturn(true, false);

         testKafkaConsumerRestDepositor.run("testTopic");
      }

      verify(injectableRestDepositor, times(1))
            .deposit("{\"depositRequests\":[{\"encodeType\":\"\",\"encodedMsg\":\"C4400000000680C0DE3\"}]}");
   }

   @Test
   public void runShouldDepositJSONEstimatedRemovalDateMessage() {
      List<ConsumerRecord<String, String>> crList = new ArrayList<>();
      crList.add(new ConsumerRecord<>("key", 0, 0, "value",
            "{\"encodedMsg\":\"C4400000000680C0DE3\",\"estimatedRemovalDate\":\"2023-12-01T17:47:11-05:15\"}"));

      Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<>();
      recordsMap.put(new TopicPartition("string", 0), crList);

      final ConsumerRecords<String, String> testConsumerRecords = new ConsumerRecords<>(recordsMap);

      when(injectableKafkaConsumer.poll(any(Duration.class))).thenReturn(testConsumerRecords);

      try (MockedStatic<LoopController> mockedLoop = mockStatic(LoopController.class)) {
         mockedLoop.when(LoopController::loop).thenReturn(true, false);

         testKafkaConsumerRestDepositor.run("testTopic");
      }

      verify(injectableRestDepositor, times(1))
            .deposit("{\"depositRequests\":[{\"encodeType\":\"\",\"encodedMsg\":\"C4400000000680C0DE3\",\"estimatedRemovalDate\":\"2023-12-01T17:47:11-05:15\"}]}");
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
