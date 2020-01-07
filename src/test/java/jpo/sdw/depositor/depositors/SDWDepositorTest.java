package jpo.sdw.depositor.depositors;

import static org.mockito.ArgumentMatchers.any;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import jpo.sdw.depositor.DepositorProperties;
import mockit.Capturing;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import reactor.core.publisher.Mono;

public class SDWDepositorTest {

   @Tested
   SDWDepositor testSDWDepositor;

   @Injectable
   WebClient injectableWebClient;

   @Injectable
   DepositorProperties depositorProperties;

   @Injectable
   JavaMailSender javaMailSender;

   @Injectable
   URI injectableURI;

   @Test
   public void testSuccess(@Mocked final LoggerFactory loggerFactory, @Capturing final Logger logger) {
      String uuid = UUID.randomUUID().toString();
      ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK).body(uuid).build();

      new Expectations() {
         {
            injectableWebClient.post().exchange();
            result = Mono.just(clientResponse);
         }
      };

      testSDWDepositor.deposit("testRequestBody");

      new Verifications() {
         {
            logger.info("Response received. Status: {}, Body: {}", HttpStatus.OK, uuid);
            javaMailSender.send(any(SimpleMailMessage.class));
            times = 0;
         }
      };
   }

   @Test
   public void testFailure(@Mocked final LoggerFactory loggerFactory, @Capturing final Logger logger) {
      String uuid = UUID.randomUUID().toString();
      ClientResponse clientResponse = ClientResponse.create(HttpStatus.I_AM_A_TEAPOT).body(uuid).build();

      new Expectations() {
         {
            injectableWebClient.post().exchange();
            result = Mono.just(clientResponse);
         }
      };

      testSDWDepositor.deposit("testRequestBody");

      new Verifications() {
         {
            logger.error("Response received. Status: {}, Body: {}", HttpStatus.I_AM_A_TEAPOT, uuid);
            javaMailSender.send((SimpleMailMessage)any);
            times = 1;
         }
      };
   }

   @Test
   public void testEmailSendFailure(@Mocked final LoggerFactory loggerFactory, @Capturing final Logger logger) {
      ClientResponse clientResponse = ClientResponse.create(HttpStatus.FORBIDDEN).body("").build();

      new Expectations() {
         {
            injectableWebClient.post().exchange();
            result = Mono.just(clientResponse);
         };
         {
            javaMailSender.send((SimpleMailMessage)any);
            result = new MailSendException("failed to send");
         }
      };

      testSDWDepositor.deposit("testRequestBody");

      new Verifications() {
         {
            logger.error("Response received. Status: {}, Body: {}", HttpStatus.FORBIDDEN, "");
            javaMailSender.send((SimpleMailMessage)any);
            logger.error("Unable to send deposit failure email: {}", "failed to send");
            times = 1;
         }
      };
   }
}
