package jpo.sdw.depositor;

import java.util.regex.Pattern;

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
   private static final String DEFAULT_DESTINATION_URL = "https://sdx-service.trihydro.com/api/deposit";
   private static final String[] DEFAULT_SUBSCRIPTION_TOPICS = { "topic.SDWDepositorInput" };
   private static final String DEFAULT_ENCODE_TYPE = "hex";

   @Autowired
   private Environment environment;

   private String groupId;
   private String encodeType;

   private String kafkaBrokers;
   private String[] subscriptionTopics;

   private String apiKey;
   private String destinationUrl;

   private String[] emailList;

   private String emailFrom;

   @PostConstruct
   void initialize() {

      if (getGroupId() == null)
         setGroupId(DEFAULT_GROUP_ID);

      if (getKafkaBrokers() == null) {
         String dockerIp = System.getenv("DOCKER_HOST_IP");
         logger.info("sdw.kafkaBrokers property not defined. Will try DOCKER_HOST_IP => {}", dockerIp);

         if (dockerIp == null) {
            logger.warn(
                  "Neither sdw.kafkaBrokers ode property nor DOCKER_HOST_IP environment variable are defined. Defaulting to localhost.");
            dockerIp = "localhost";
         }
         setKafkaBrokers(dockerIp + ":" + DEFAULT_KAFKA_PORT);
      }

      if (getEncodeType() == null)
         setEncodeType(DEFAULT_ENCODE_TYPE);

      if (getDestinationUrl() == null)
         setDestinationUrl(DEFAULT_DESTINATION_URL);

      if (getSubscriptionTopics() == null || getSubscriptionTopics().length == 0) {
         String topics = String.join(",", DEFAULT_SUBSCRIPTION_TOPICS);
         logger.info("No Kafka subscription topics specified in configuration, defaulting to {}", topics);
         subscriptionTopics = DEFAULT_SUBSCRIPTION_TOPICS;
      }

      if (getApiKey() == null || getApiKey().isEmpty()) {
         logger.error("No API Key specified in configuration");
         throw new IllegalArgumentException("No API Key specified in configuration");
      }

      if (getEmailList() == null || getEmailList().length == 0) {
         logger.error("No error email list specified in configuration");
         throw new IllegalArgumentException("No error email list specified in configuration");
      }

      if (getEmailFrom() == null || getEmailFrom().isEmpty()) {
         logger.error("No from email specified in configuration");
         throw new IllegalArgumentException("No from email specified in configuration");
      }

      if (!emailValid()) {
         logger.error("From email is not a valid email address");
         throw new IllegalArgumentException("From email is not a valid email address");
      }

      if (!emailListValid()) {
         logger.error("Email list is not valid email address(es)");
         throw new IllegalArgumentException("Email list is not valid email address(es)");
      }
   }

   private boolean emailValid() {
      String emailRegex = "^[\\w+-.%]+@[\\w-]+\\.[A-Za-z]{2,4}$";

      Pattern pat = Pattern.compile(emailRegex);
      if (getEmailFrom() == null)
         return false;
      return pat.matcher(getEmailFrom()).matches();
   }

   private boolean emailListValid() {
      String emailRegex = "^([\\w+-.%]+@[\\w-]+\\.[A-Za-z]{2,4},?)+$";
      Pattern pat = Pattern.compile(emailRegex);
      if (getEmailList() == null)
         return false;
      return pat.matcher(String.join(",", getEmailList())).matches();
   }

   public String getEmailFrom() {
      return emailFrom;
   }

   public void setEmailFrom(String emailFrom) {
      this.emailFrom = emailFrom;
   }

   public String[] getEmailList() {
      return emailList;
   }

   public void setEmailList(String[] emailList) {
      this.emailList = emailList;
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

   public String[] getSubscriptionTopics() {
      return subscriptionTopics;
   }

   public void setSubscriptionTopics(String[] subscriptionTopics) {
      this.subscriptionTopics = subscriptionTopics;
   }

   public String getGroupId() {
      return groupId;
   }

   public void setGroupId(String groupId) {
      this.groupId = groupId;
   }

   public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
   }

   public String getApiKey() {
      return apiKey;
   }

   public String getEncodeType() {
      return encodeType;
   }

   public void setEncodeType(String encodeType) {
      this.encodeType = encodeType;
   }
}
