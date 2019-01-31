package jpo.sdw.depositor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Test;

import jpo.sdw.depositor.DepositorProperties;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Mocked;

public class KafkaConsumerFactoryTest {

   @Capturing
   KafkaConsumer<?, ?> capturingKafkaConsumer;

   @Mocked
   DepositorProperties mockedDepositorProperties;

   @Test
   public void createConsumerShouldCreateConsumer() {
      new Expectations() {
         {
            // These are required because Properties throws NPE when values are null
            mockedDepositorProperties.getKafkaBrokers();
            result = "kafkaBrokers";

            mockedDepositorProperties.getGroupId();
            result = "groupId";

         }
      };
      assertNotNull(KafkaConsumerFactory.createConsumer(mockedDepositorProperties));
   }

   @Test
   public void testConstructorIsPrivate()
         throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      Constructor<KafkaConsumerFactory> constructor = KafkaConsumerFactory.class.getDeclaredConstructor();
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
