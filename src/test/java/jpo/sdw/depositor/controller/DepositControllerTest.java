package jpo.sdw.depositor.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class DepositControllerTest {

   @Tested
   DepositController testDepositController;

   @Injectable
   DepositorProperties injectableDepositorProperties;

   @Capturing
   KafkaConsumerFactory capturingKafkaConsumerFactory;

   @Capturing
   KafkaConsumerRestDepositor capturingKafkaConsumerRestDepositor;
   @Capturing
   BasicAuthorizationInterceptor capturingBasicAuthorizationInterceptor;
   @Capturing
   URI capturingURI;

   @Mocked
   BasicAuthorizationInterceptor mockBasicAuthorizationInterceptor;

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
