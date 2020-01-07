package jpo.sdw.depositor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.springframework.core.env.Environment;

import mockit.Mocked;

public class DepositorPropertiesTest {

   @Mocked
   Environment mockEnvironment;

   @Test
   public void testAllValuesAlreadySetOneTopic() {

      String expectedKafkaBrokers = "testKafkaBrokers";
      String[] expectedSubscriptionTopics = { "testSubscriptionTopic" };
      String expectedDestinationUrl = "testDestinationUrl";
      String expectedGroupId = "testGroupId";
      String expectedApiKey = "apikey1";
      String expectedFromEmail = "test@test.com";
      String[] expectedEmailList = new String[] {"test@test.com", "unit@test.com"};

      DepositorProperties testDepositorProperties = new DepositorProperties();

      testDepositorProperties.setKafkaBrokers(expectedKafkaBrokers);
      testDepositorProperties.setSubscriptionTopics(expectedSubscriptionTopics);
      testDepositorProperties.setDestinationUrl(expectedDestinationUrl);
      testDepositorProperties.setGroupId(expectedGroupId);
      testDepositorProperties.setEnvironment(mockEnvironment);
      testDepositorProperties.setApiKey(expectedApiKey);
      testDepositorProperties.setEmailList(expectedEmailList);
      testDepositorProperties.setEmailFrom(expectedFromEmail);

      testDepositorProperties.initialize();

      assertEquals("Incorrect kafkaBrokers", expectedKafkaBrokers, testDepositorProperties.getKafkaBrokers());
      assertEquals("Incorrect subscriptionTopic", expectedSubscriptionTopics[0],
            testDepositorProperties.getSubscriptionTopics()[0]);
      assertEquals("Incorrect destinationUrl", expectedDestinationUrl, testDepositorProperties.getDestinationUrl());
      assertEquals("Incorrect groupId", expectedGroupId, testDepositorProperties.getGroupId());
      assertNotNull("No environment", testDepositorProperties.getEnvironment());
      assertEquals("Incorrect apikey", expectedApiKey, testDepositorProperties.getApiKey());assertEquals("Incorrect from email", expectedFromEmail, testDepositorProperties.getEmailFrom());
      assertArrayEquals("Incorrect email list", expectedEmailList, testDepositorProperties.getEmailList());
   }

   @Test
   public void testAllValuesAlreadySetMultipleTopics() {

      String expectedKafkaBrokers = "testKafkaBrokers";
      String[] expectedSubscriptionTopics = { "testSubscriptionTopic0", "testSubscriptionTopic1",
            "testSubscriptionTopic2" };
      String expectedDestinationUrl = "testDestinationUrl";
      String expectedGroupId = "testGroupId";
      String expectedApiKey = "apikey1";
      String expectedFromEmail = "test@test.com";
      String[] expectedEmailList = {"test@test.com", "unit@test.com"};

      DepositorProperties testDepositorProperties = new DepositorProperties();

      testDepositorProperties.setKafkaBrokers(expectedKafkaBrokers);
      testDepositorProperties.setSubscriptionTopics(expectedSubscriptionTopics);
      testDepositorProperties.setDestinationUrl(expectedDestinationUrl);
      testDepositorProperties.setGroupId(expectedGroupId);
      testDepositorProperties.setEnvironment(mockEnvironment);
      testDepositorProperties.setApiKey(expectedApiKey);
      testDepositorProperties.setEmailList(expectedEmailList);
      testDepositorProperties.setEmailFrom(expectedFromEmail);

      testDepositorProperties.initialize();

      assertEquals("Incorrect kafkaBrokers", expectedKafkaBrokers, testDepositorProperties.getKafkaBrokers());
      assertEquals("Incorrect subscriptionTopic0", expectedSubscriptionTopics[0],
            testDepositorProperties.getSubscriptionTopics()[0]);
      assertEquals("Incorrect subscriptionTopic1", expectedSubscriptionTopics[1],
            testDepositorProperties.getSubscriptionTopics()[1]);
      assertEquals("Incorrect subscriptionTopic2", expectedSubscriptionTopics[2],
            testDepositorProperties.getSubscriptionTopics()[2]);
      assertEquals("Incorrect destinationUrl", expectedDestinationUrl, testDepositorProperties.getDestinationUrl());
      assertEquals("Incorrect groupId", expectedGroupId, testDepositorProperties.getGroupId());
      assertNotNull("No environment", testDepositorProperties.getEnvironment());
      assertEquals("Incorrect apikey", expectedApiKey, testDepositorProperties.getApiKey());
      assertEquals("Incorrect from email", expectedFromEmail, testDepositorProperties.getEmailFrom());
      assertArrayEquals("Incorrect email list", expectedEmailList, testDepositorProperties.getEmailList());
   }

   @Test
   public void testDefaults() {
      DepositorProperties testDepositorProperties = new DepositorProperties();

      testDepositorProperties.setApiKey("apikey1");
      testDepositorProperties.setEmailList(new String[] {"test@test.com", "unit@test.com"});
      testDepositorProperties.setEmailFrom("test@test.com");
      testDepositorProperties.setSubscriptionTopics(new String[] { "topic.Topic" });

      testDepositorProperties.initialize();

      assertNotNull(testDepositorProperties.getKafkaBrokers());
      assertNotNull(testDepositorProperties.getDestinationUrl());
      assertNotNull(testDepositorProperties.getGroupId());
   }

   @Test
   public void nullApiKeyThrowsException() {
      DepositorProperties testDepositorProperties = new DepositorProperties();
      testDepositorProperties.setEmailList(new String[] {"test@test.com", "unit@test.com"});
      testDepositorProperties.setEmailFrom("test@test.com");
      try {
         testDepositorProperties.initialize();
         fail("Expected IllegalArgumentException");
      } catch (Exception e) {
         assertTrue(e instanceof IllegalArgumentException);
         assertEquals("No API Key specified in configuration", e.getMessage());
      }
   }

   @Test
   public void emptyApiKeyThrowsException() {
      DepositorProperties testDepositorProperties = new DepositorProperties();
      testDepositorProperties.setApiKey("");
      testDepositorProperties.setEmailList(new String[] {"test@test.com", "unit@test.com"});
      testDepositorProperties.setEmailFrom("test@test.com");
      try {
         testDepositorProperties.initialize();
         fail("Expected IllegalArgumentException");
      } catch (Exception e) {
         assertTrue(e instanceof IllegalArgumentException);
         assertEquals("No API Key specified in configuration", e.getMessage());
      }
   }

   @Test
   public void invalidFromEmailThrowsException() {
      DepositorProperties testDepositorProperties = new DepositorProperties();
      testDepositorProperties.setApiKey("apikey1");
      testDepositorProperties.setEmailList(new String[] {"test@test.com", "unit@test.com"});
      testDepositorProperties.setEmailFrom("test@test..com");
      try {
         testDepositorProperties.initialize();
         fail("Expected IllegalArgumentException");
      } catch (Exception e) {
         assertTrue(e instanceof IllegalArgumentException);
         assertEquals("From email is not a valid email address", e.getMessage());
      }
   }

   @Test
   public void invalidEmailListThrowsException() {
      DepositorProperties testDepositorProperties = new DepositorProperties();
      testDepositorProperties.setApiKey("apikey1");
      testDepositorProperties.setEmailList(new String[] {"test@test...com", "unit@test.com"});
      testDepositorProperties.setEmailFrom("test@test.com");
      try {
         testDepositorProperties.initialize();
         fail("Expected IllegalArgumentException");
      } catch (Exception e) {
         assertTrue(e instanceof IllegalArgumentException);
         assertEquals("Email list is not valid email address(es)", e.getMessage());
      }
   }
}
