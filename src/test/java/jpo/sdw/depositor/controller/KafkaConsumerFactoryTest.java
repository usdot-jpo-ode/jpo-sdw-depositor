package jpo.sdw.depositor.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import jpo.sdw.depositor.DepositorProperties;

@RunWith(MockitoJUnitRunner.class)
public class KafkaConsumerFactoryTest {

   @Mock
   DepositorProperties mockedDepositorProperties;


   @Test
   public void createConsumerShouldCreateConsumer() {
      when(mockedDepositorProperties.getKafkaBrokers()).thenReturn("kafkaBrokers");
      when(mockedDepositorProperties.getGroupId()).thenReturn("groupId");

      try (MockedConstruction<KafkaConsumer> mocked = mockConstruction(KafkaConsumer.class)) {
         assertNotNull(KafkaConsumerFactory.createConsumer(mockedDepositorProperties));
      }
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
