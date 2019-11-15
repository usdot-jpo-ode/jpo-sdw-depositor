package jpo.sdw.depositor.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSender;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

public class DepositControllerTest {

   @Tested
   DepositController testDepositController;

   @Injectable
   DepositorProperties injectableDepositorProperties;

   @Injectable
   JavaMailSender sender;

   @Capturing
   KafkaConsumerFactory capturingKafkaConsumerFactory;

   @Capturing
   KafkaConsumerRestDepositor capturingKafkaConsumerRestDepositor;
   
   @Capturing
   URI capturingURI;

   @Test
   public void shouldRun() throws URISyntaxException {
      new Expectations() {
         {
            capturingKafkaConsumerRestDepositor.run((String[]) any);
            times = 1;
         }
      };
      testDepositController.run();
   }
}
