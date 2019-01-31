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

   private static final String DEFAULT_GROUP_ID = "usdot.jpo.sdw";

   private static final String DEFAULT_KAFKA_PORT = "9092";
   private static final String DEFAULT_KAFKA_SUBSCRIPTION_TOPIC = "topic.J2735TimBroadcastJson";

   private static final String DEFAULT_DESTINATION_URL = "http://localhost:8082/sdw";

   @Autowired
   private Environment environment;

   private String groupId;

   private String kafkaBrokers;
   private String subscriptionTopic;

   private String username;
   private String password;

   private String destinationUrl;

   @PostConstruct
   void initialize() {

      logger.info("Values: {} {} {} {}", groupId, kafkaBrokers, subscriptionTopic, destinationUrl);
      if (getGroupId() == null)
         setGroupId(DEFAULT_GROUP_ID);

      if (getKafkaBrokers() == null) {
         String dockerIp = System.getenv("DOCKER_HOST_IP");
         logger.info("sdw.kafkaBrokers property not defined. Will try DOCKER_HOST_IP => {}", dockerIp);

         if (dockerIp == null) {
            logger.warn(
                  "Neither sdw.kafkaBrokers ode property nor DOCKER_HOST_IP environment variable are defined. Defaulting to localhost.");
            dockerIp = DEFAULT_DESTINATION_URL;
         }
         setKafkaBrokers(dockerIp + ":" + DEFAULT_KAFKA_PORT);
      }
      if (getSubscriptionTopic() == null)
         setSubscriptionTopic(DEFAULT_KAFKA_SUBSCRIPTION_TOPIC);

      if (getUsername() == null) {
         logger.error("No username specified in configuration");
         throw new IllegalArgumentException("No username specified in configuration");
      }

      if (getPassword() == null) {
         logger.error("No password specified in configuration");
         throw new IllegalArgumentException("No password specified in configuration");
      }

      if (getDestinationUrl() == null)
         setDestinationUrl(DEFAULT_DESTINATION_URL);

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

   public String getGroupId() {
      return groupId;
   }

   public void setGroupId(String groupId) {
      this.groupId = groupId;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String sdwUsername) {
      this.username = sdwUsername;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String sdwPassword) {
      this.password = sdwPassword;
   }
}
