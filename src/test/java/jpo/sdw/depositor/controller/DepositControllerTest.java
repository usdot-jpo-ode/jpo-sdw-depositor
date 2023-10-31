package jpo.sdw.depositor.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSender;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Mocked;


public class DepositControllerTest {

   @Tested
   DepositController testDepositController;

   @Injectable
   DepositorProperties injectableDepositorProperties;

   @Injectable
   JavaMailSender sender;

   @Mocked
   KafkaConsumerFactory mockedCapturingKafkaConsumerFactory;

   @Mocked
   KafkaConsumerRestDepositor mockedCapturingKafkaConsumerRestDepositor;
   
   @Mocked
   URI mockedCapturingURI;

   @Test
   public void shouldRun() throws URISyntaxException {

      new Expectations() {
         {
            injectableDepositorProperties.getDestinationUrl(); result = "127.0.0.1";

            mockedCapturingKafkaConsumerRestDepositor.run((String[]) any);
            times = 1;
         }
      };
      testDepositController.run();
   }
}
