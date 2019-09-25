package jpo.sdw.depositor.depositors;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class SDWDepositor extends RestDepositor<String> {

   private static final Logger logger = LoggerFactory.getLogger(SDWDepositor.class);

   public SDWDepositor(RestTemplate restTemplate, URI destination) {
      super(restTemplate, destination);
   }

   @Override
   public void deposit(String message) {
      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);

         HttpEntity<String> httpEntity = new HttpEntity<String>(message, headers);

         ResponseEntity<String> result = this.getRestTemplate().postForEntity(this.getDestination(), httpEntity,
               String.class);

         logger.info("Response received. Status: {}, Body: {}", result.getStatusCode(), result.getBody());
      } catch (ResourceAccessException e) {
         logger.error("Failed to send message to destination", e);
      }
   }
}
