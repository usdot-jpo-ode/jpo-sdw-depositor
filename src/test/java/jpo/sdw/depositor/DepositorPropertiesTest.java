package jpo.sdw.depositor;

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
   public void testAllValuesAlreadySet() {

      String expectedKafkaBrokers = "testKafkaBrokers";
      String expectedSubscriptionTopic = "testSubscriptionTopic";
      String expectedDestinationUrl = "testDestinationUrl";
      String expectedGroupId = "testGroupId";
      String expectedUsername = "testUsername";
      String expectedPassword = "testPassword";

      DepositorProperties testDepositorProperties = new DepositorProperties();

      testDepositorProperties.setKafkaBrokers(expectedKafkaBrokers);
      testDepositorProperties.setSubscriptionTopic(expectedSubscriptionTopic);
      testDepositorProperties.setDestinationUrl(expectedDestinationUrl);
      testDepositorProperties.setGroupId(expectedGroupId);
      testDepositorProperties.setEnvironment(mockEnvironment);
      testDepositorProperties.setUsername(expectedUsername);
      testDepositorProperties.setPassword(expectedPassword);

      testDepositorProperties.initialize();

      assertEquals("Incorrect kafkaBrokers", expectedKafkaBrokers, testDepositorProperties.getKafkaBrokers());
      assertEquals("Incorrect subscriptionTopic", expectedSubscriptionTopic,
            testDepositorProperties.getSubscriptionTopic());
      assertEquals("Incorrect destinationUrl", expectedDestinationUrl, testDepositorProperties.getDestinationUrl());
      assertEquals("Incorrect groupId", expectedGroupId, testDepositorProperties.getGroupId());
      assertNotNull("No environment", testDepositorProperties.getEnvironment());
      assertEquals("Incorrect username", expectedUsername, testDepositorProperties.getUsername());
      assertEquals("Incorrect password", expectedPassword, testDepositorProperties.getPassword());
   }

   @Test
   public void testDefaults() {

      DepositorProperties testDepositorProperties = new DepositorProperties();

      testDepositorProperties.setUsername("uuuuuuuu");
      testDepositorProperties.setPassword("pppppppp");

      testDepositorProperties.initialize();

      assertNotNull(testDepositorProperties.getKafkaBrokers());
      assertNotNull(testDepositorProperties.getSubscriptionTopic());
      assertNotNull(testDepositorProperties.getDestinationUrl());
      assertNotNull(testDepositorProperties.getGroupId());
   }

   @Test
   public void missingUsernameThrowsException() {
      DepositorProperties testDepositorProperties = new DepositorProperties();
      testDepositorProperties.setPassword("pppppppp");
      try {
         testDepositorProperties.initialize();
         fail("Expected IllegalArgumentException");
      } catch (Exception e) {
         assertTrue(e instanceof IllegalArgumentException);
         assertEquals("No username specified in configuration", e.getMessage());
      }
   }

   @Test
   public void missingPasswordThrowsException() {
      DepositorProperties testDepositorProperties = new DepositorProperties();
      testDepositorProperties.setUsername("uuuuuuuu");
      try {
         testDepositorProperties.initialize();
         fail("Expected IllegalArgumentException");
      } catch (Exception e) {
         assertTrue(e instanceof IllegalArgumentException);
         assertEquals("No password specified in configuration", e.getMessage());
      }
   }

}
