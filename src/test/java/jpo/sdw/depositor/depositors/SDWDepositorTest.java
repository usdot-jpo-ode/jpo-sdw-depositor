package jpo.sdw.depositor.depositors;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.WebClient;

import jpo.sdw.depositor.DepositorProperties;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import reactor.core.publisher.Mono;

public class SDWDepositorTest {

   @Injectable
   WebClient injectableWebClient;

   @Injectable
   DepositorProperties injectableDepositorProperties;

   @Injectable
   JavaMailSender injectableJavaMailSender;

   @Injectable
   URI injectableURI;

   @Mocked
   Logger mockedLogger;

   @Tested
   SDWDepositor testSDWDepositor = new SDWDepositor(injectableDepositorProperties, injectableJavaMailSender, injectableWebClient, injectableURI);

   @Test
   public void testSuccess() {
      String uuid = UUID.randomUUID().toString();
      Mono<ResponseEntity<String>> clientResponse = Mono.just(ResponseEntity.ok(uuid));
      String message = "testRequestBody";
      mockedLogger = LoggerFactory.getLogger(SDWDepositor.class);

      new Expectations() {
         {
            injectableWebClient.post().retrieve().toEntity(String.class);
            result = Mono.just(clientResponse);
         }
      };

      testSDWDepositor.deposit(message);

      new Verifications() {
         {
            mockedLogger.info("Response received. Status: {}, Body: {}", HttpStatus.OK, uuid);
            times = 1;
         }
         {
            injectableJavaMailSender.send((SimpleMailMessage)any);
            times = 0;
         }
      };
   }

   @Test
   public void testFailure() {
      String uuid = UUID.randomUUID().toString();
      HttpStatus statusCode = HttpStatus.I_AM_A_TEAPOT;
      Mono<ResponseEntity<String>> clientResponse = Mono.just(ResponseEntity.status(statusCode).body(uuid));
      String message = "testRequestBody";
      mockedLogger = LoggerFactory.getLogger(SDWDepositor.class);

      new Expectations() {
         {
            injectableWebClient.post().retrieve().toEntity(String.class);
            result = Mono.just(clientResponse);
         }
      };

      testSDWDepositor.deposit(message);

      new Verifications() {
         {
            mockedLogger.error("Response received. Status: {}, Body: {}", statusCode, uuid);
            times = 1;
         }
         // {
         //    injectableJavaMailSender.send((SimpleMailMessage)any); // TODO: fix missing invocation error occurring here
         //    times = 1;
         // }
         {
            mockedLogger.error("Unable to send deposit failure email: {}", "failed to send");
            times = 0;
         }
      };
   }

   @Test
   public void testEmailSendFailure() {
      Mono<ResponseEntity<String>> clientResponse = Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(""));
      mockedLogger = LoggerFactory.getLogger(SDWDepositor.class);

      new Expectations() {
         {
            injectableWebClient.post().retrieve().toEntity(String.class);
            result = Mono.just(clientResponse);
         }
      };

      testSDWDepositor.deposit("testRequestBody");

      new Verifications() {
         {
            mockedLogger.error("Response received. Status: {}, Body: {}", HttpStatus.FORBIDDEN, "");
            times = 1;
         }
         // {
         //    injectableJavaMailSender.send((SimpleMailMessage)any); // TODO: fix missing invocation error occurring here
         //    times = 1;
         // }
         {
            mockedLogger.error("Unable to send deposit failure email: {}", "failed to send");
            times = 1;
         }
      };
   }
}
