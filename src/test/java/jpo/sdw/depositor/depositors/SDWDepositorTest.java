package jpo.sdw.depositor.depositors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;

import jpo.sdw.depositor.DepositorProperties;
import nl.altindag.log.LogCaptor;
import reactor.core.publisher.Mono;

@RunWith(MockitoJUnitRunner.class)
public class SDWDepositorTest {

   @Mock
   WebClient webClient;

   @Mock
   WebClient.RequestBodyUriSpec requestBodyUriSpec;

   @SuppressWarnings("rawtypes")
   @Mock
   WebClient.RequestHeadersSpec requestHeadersSpec;

   @Mock
   WebClient.ResponseSpec responseSpec;

   @Mock
   DepositorProperties depositorProperties;

   @Mock
   JavaMailSender javaMailSender;

   URI destination;

   SDWDepositor sdwDepositor;

   LogCaptor logCaptor;

   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Before
   public void before() throws Exception {
      destination = new URI("http://example.com");
      sdwDepositor = new SDWDepositor(depositorProperties, javaMailSender, webClient, destination);

      doReturn(requestBodyUriSpec).when(webClient).post();
      doReturn(requestHeadersSpec).when(requestBodyUriSpec).body(any(BodyInserter.class));
      doReturn(responseSpec).when(requestHeadersSpec).retrieve();

      logCaptor = LogCaptor.forClass(SDWDepositor.class);
   }

   @After
   public void after() {
      if (logCaptor != null) {
         logCaptor.close();
      }
   }

   @Test
   public void testSuccess() {
      String uuid = UUID.randomUUID().toString();
      doReturn(Mono.just(ResponseEntity.ok(uuid)))
            .when(responseSpec).toEntity(String.class);

      sdwDepositor.deposit("testRequestBody");

      assertThat(logCaptor.getInfoLogs(),
            hasItem(equalTo("Response received. Status: " + HttpStatus.OK + ", Body: " + uuid)));
      verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
   }

   @Test
   public void testFailure() {
      String uuid = UUID.randomUUID().toString();
      HttpStatus statusCode = HttpStatus.I_AM_A_TEAPOT;
      doReturn(Mono.just(ResponseEntity.status(statusCode).body(uuid)))
            .when(responseSpec).toEntity(String.class);

      sdwDepositor.deposit("testRequestBody");

      assertThat(logCaptor.getErrorLogs(),
            hasItem(equalTo("Response received. Status: " + statusCode + ", Body: " + uuid)));
      verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
   }

   @Test
   public void testEmailSendFailure() {
      doReturn(Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("")))
            .when(responseSpec).toEntity(String.class);
      doThrow(new RuntimeException("failed to send"))
            .when(javaMailSender).send(any(SimpleMailMessage.class));

      sdwDepositor.deposit("testRequestBody");

      assertThat(logCaptor.getErrorLogs(),
            hasItem(equalTo("Response received. Status: " + HttpStatus.FORBIDDEN + ", Body: ")));
      assertThat(logCaptor.getErrorLogs(),
            hasItem(containsString("failed to send")));
      verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
   }
}
