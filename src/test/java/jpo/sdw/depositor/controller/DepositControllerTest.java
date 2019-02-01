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

public class DepositControllerTest {

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

   @Test
   public void shouldRun() throws URISyntaxException {
      new Expectations() {
         {
            capturingKafkaConsumerRestDepositor.run((String[]) any);
            times = 1;
         }
      };
      new DepositController(injectableDepositorProperties);
   }

}
