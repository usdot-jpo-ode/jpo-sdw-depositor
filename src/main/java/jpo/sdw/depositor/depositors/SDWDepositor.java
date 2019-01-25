package jpo.sdw.depositor.depositors;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SDWDepositor extends RestDepositor<String> {

   private static final Logger logger = LoggerFactory.getLogger(SDWDepositor.class);

   public SDWDepositor(RestTemplate restTemplate, URI destination) {
      super(restTemplate, destination);
   }

   @Override
   public void deposit(String requestBody) {
      ResponseEntity<String> result = this.getRestTemplate().postForEntity(this.getDestination(), requestBody,
            String.class);
      logger.info("Response received. Status: {}, Body: {}", result.getStatusCode(), result.getBody());
   }

}
