package jpo.sdw.depositor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor;


@RunWith(MockitoJUnitRunner.class)
public class DepositControllerTest {

   @Mock
   DepositorProperties injectableDepositorProperties;

   @Mock
   JavaMailSender sender;

   @Test
   public void shouldRun() throws URISyntaxException {
      when(injectableDepositorProperties.getDestinationUrl()).thenReturn("127.0.0.1");

      try (MockedStatic<KafkaConsumerFactory> mockedFactory = mockStatic(KafkaConsumerFactory.class);
           MockedConstruction<KafkaConsumerRestDepositor> mockedConsumer =
               mockConstruction(KafkaConsumerRestDepositor.class)) {

         DepositController testDepositController = new DepositController(injectableDepositorProperties, sender);
         testDepositController.run();

         KafkaConsumerRestDepositor constructedConsumer = mockedConsumer.constructed().get(0);
         verify(constructedConsumer, times(1)).run(any(String[].class));
      }
   }
}
