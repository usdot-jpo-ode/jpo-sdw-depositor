package jpo.sdw.depositor;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@ConfigurationProperties("sdw")
@PropertySource("classpath:application.properties")
public class DepositorProperties implements EnvironmentAware {

   private static final Logger logger = LoggerFactory.getLogger(DepositorProperties.class);

   private static final String DEFAULT_KAFKA_PORT = "9092";
   private static final String DEFAULT_DESTINATION_URL = "localhost";
   private static final String DEFAULT_KAFKA_TOPIC = "test.topic";

   @Autowired
   private Environment environment;

   private String destinationUrl;
   private String kafkaBrokers;
   private String subscriptionTopic;

   public DepositorProperties() {

   }

   @PostConstruct
   void initialize() {
      if (getKafkaBrokers() == null) {

         logger.info("ode.kafkaBrokers property not defined. Will try DOCKER_HOST_IP => {}", getKafkaBrokers());

         String dockerIp = System.getenv("DOCKER_HOST_IP");

         if (dockerIp == null) {
            logger.warn(
                  "Neither ode.kafkaBrokers ode property nor DOCKER_HOST_IP environment variable are defined. Defaulting to localhost.");
            dockerIp = "localhost";
         }
         setKafkaBrokers(dockerIp + ":" + DEFAULT_KAFKA_PORT);
         setDestinationUrl(DEFAULT_DESTINATION_URL);
         setSubscriptionTopic(DEFAULT_KAFKA_TOPIC);
      }
   }

   @Override
   public void setEnvironment(Environment environment) {
      this.environment = environment;
   }

   public Environment getEnvironment() {
      return environment;
   }

   public String getDestinationUrl() {
      return destinationUrl;
   }

   public void setDestinationUrl(String destinationUrl) {
      this.destinationUrl = destinationUrl;
   }

   public String getKafkaBrokers() {
      return kafkaBrokers;
   }

   public void setKafkaBrokers(String kafkaBrokers) {
      this.kafkaBrokers = kafkaBrokers;
   }

   public String getSubscriptionTopic() {
      return subscriptionTopic;
   }

   public void setSubscriptionTopic(String subscriptionTopic) {
      this.subscriptionTopic = subscriptionTopic;
   }
}
