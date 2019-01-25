package jpo.sdw.depositor.depositors;

import java.net.URI;

import org.springframework.web.client.RestTemplate;

public abstract class RestDepositor<T extends Object> implements Depositor<T> {

   private RestTemplate restTemplate;
   private URI destination;

   public RestDepositor(RestTemplate restTemplate, URI destination) {
      this.setRestTemplate(restTemplate);
      this.setDestination(destination);
   }

   public RestTemplate getRestTemplate() {
      return restTemplate;
   }

   public void setRestTemplate(RestTemplate restTemplate) {
      this.restTemplate = restTemplate;
   }

   public URI getDestination() {
      return destination;
   }

   public void setDestination(URI destination) {
      this.destination = destination;
   }

}
