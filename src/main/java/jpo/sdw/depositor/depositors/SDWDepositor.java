package jpo.sdw.depositor.depositors;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import jpo.sdw.depositor.DepositorProperties;
import reactor.core.publisher.Mono;

public class SDWDepositor extends RestDepositor<String> {

   private DepositorProperties depositorProperties;
   private JavaMailSender javaMailSender;

   private static final Logger logger = LoggerFactory.getLogger(SDWDepositor.class);

   public SDWDepositor(DepositorProperties props, JavaMailSender sender, WebClient webClient, URI destination) {
      super(webClient, destination);
      depositorProperties = props;
      javaMailSender = sender;
   }

   @Override
   public void deposit(String message) {
      Mono<ClientResponse> clientResponse = this.getWebClient().post().body(BodyInserters.fromObject(message))
            .exchange();

      clientResponse.subscribe(response -> {
         HttpStatus statusCode = response.statusCode();

         response.bodyToMono(String.class).subscribe(body -> {
            if (statusCode != HttpStatus.OK) {
               // There was an error with depositing data, email the team
               logger.error("Response received. Status: {}, Body: {}", statusCode, body);
               SimpleMailMessage msg = new SimpleMailMessage();
               msg.setTo(depositorProperties.getEmailList());
               msg.setFrom(depositorProperties.getEmailFrom());
               msg.setSubject("ODE Failed to Deposit to SDX");
               msg.setText(String.format("Status: %d, Body: %s", statusCode.value(), body));

               try {
                  javaMailSender.send(msg);
               } catch (Exception e) {
                  logger.error("Unable to send deposit failure email: {}", e.getMessage());
                  e.printStackTrace();
               }
            } else {
               logger.info("Response received. Status: {}, Body: {}", statusCode, body);
            }
         });
      });
   }
}
