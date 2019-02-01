package jpo.sdw.depositor.depositors;

import java.net.URI;

import org.junit.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

public class SDWDepositorTest {

   @Tested
   SDWDepositor testSDWDepositor;

   @Injectable
   RestTemplate injectableRestTemplate;

   @Injectable
   URI injectableURI;

   @Test
   public void testSuccess() {
      testSDWDepositor.deposit("testRequestBody");
   }

   @Test
   public void testFailure() {

      new Expectations() {
         {
            injectableRestTemplate.postForEntity(injectableURI, "testRequestBody", String.class);
            result = new ResourceAccessException("testException123");
         }
      };

      testSDWDepositor.deposit("testRequestBody");
   }

}
