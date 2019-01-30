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
   private static final String DEFAULT_DESTINATION_URL = "http://localhost";
   private static final String DEFAULT_DESTINATION_PORT = "8082";
   private static final String DEFAULT_KAFKA_TOPIC = "topic.J2735TimBroadcastJson";

   @Autowired
   private Environment environment;

   private String destinationUrl;
   private String kafkaBrokers;
   private String subscriptionTopic;
   private String destinationPort;

   public DepositorProperties() {

   }

   @PostConstruct
   void initialize() {
      if (getKafkaBrokers() == null) {
         String dockerIp = System.getenv("DOCKER_HOST_IP");
         logger.info("sdw.kafkaBrokers property not defined. Will try DOCKER_HOST_IP => {}", dockerIp);

         if (dockerIp == null) {
            logger.warn(
                  "Neither ode.kafkaBrokers ode property nor DOCKER_HOST_IP environment variable are defined. Defaulting to localhost.");
            dockerIp = "localhost";
         }
         setKafkaBrokers(dockerIp + ":" + DEFAULT_KAFKA_PORT);
      }

      if (getDestinationUrl() == null)
         setDestinationUrl(DEFAULT_DESTINATION_URL);
      if (getDestinationPort() == null)
         setDestinationPort(DEFAULT_DESTINATION_PORT);
      if (getSubscriptionTopic() == null)
         setSubscriptionTopic(DEFAULT_KAFKA_TOPIC);
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

   public String getDestinationPort() {
      return destinationPort;
   }

   public void setDestinationPort(String destinationPort) {
      this.destinationPort = destinationPort;
   }
}
