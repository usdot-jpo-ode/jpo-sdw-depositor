package jpo.sdw.depositor.depositors;

import java.net.URI;

import org.springframework.web.reactive.function.client.WebClient;

public abstract class RestDepositor<T extends Object> implements Depositor<T> {

   private WebClient webClient;
   private URI destination;

   public RestDepositor(WebClient webClient, URI destination) {
      this.setWebClient(webClient);
      this.setDestination(destination);
   }

   public WebClient getWebClient() {
      return webClient;
   }

   public void setWebClient(WebClient webClient) {
      this.webClient = webClient;
   }

   public URI getDestination() {
      return destination;
   }

   public void setDestination(URI destination) {
      this.destination = destination;
   }

}
