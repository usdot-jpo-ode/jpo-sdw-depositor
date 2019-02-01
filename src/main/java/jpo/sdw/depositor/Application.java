package jpo.sdw.depositor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DepositorProperties.class)
public class Application {

   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }
}