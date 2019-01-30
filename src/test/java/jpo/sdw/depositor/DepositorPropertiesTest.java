package jpo.sdw.depositor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.core.env.Environment;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class DepositorPropertiesTest {

   @Tested
   DepositorProperties testDepositorProperties;

   @Injectable
   Environment injectableEnvironment;

   @Mocked
   Environment mockEnvironment;

   @Test
   public void testAllValuesAlreadySet() {

      String expectedKafkaBrokers = "testKafkaBrokers";
      String expectedSubscriptionTopic = "testSubscriptionTopic";
      String expectedDestinationProtocol = "testDestinationProtocol";
      String expectedDestinationUrl = "testDestinationUrl";
      String expectedDestinationPort = "testDestinationPort";
      String expectedDestinationEndpoint = "testDestinationEndpoint";
      String expectedGroupId = "testGroupId";

      testDepositorProperties.setKafkaBrokers(expectedKafkaBrokers);
      testDepositorProperties.setSubscriptionTopic(expectedSubscriptionTopic);
      testDepositorProperties.setDestinationProtocol(expectedDestinationProtocol);
      testDepositorProperties.setDestinationUrl(expectedDestinationUrl);
      testDepositorProperties.setDestinationPort(expectedDestinationPort);
      testDepositorProperties.setDestinationEndpoint(expectedDestinationEndpoint);
      testDepositorProperties.setGroupId(expectedGroupId);
      testDepositorProperties.setEnvironment(mockEnvironment);
      testDepositorProperties.initialize();

      assertEquals("Incorrect kafkaBrokers", expectedKafkaBrokers, testDepositorProperties.getKafkaBrokers());
      assertEquals("Incorrect subscriptionTopic", expectedSubscriptionTopic,
            testDepositorProperties.getSubscriptionTopic());
      assertEquals("Incorrect destinationProtocol", expectedDestinationProtocol,
            testDepositorProperties.getDestinationProtocol());
      assertEquals("Incorrect destinationUrl", expectedDestinationUrl, testDepositorProperties.getDestinationUrl());
      assertEquals("Incorrect destinationPort", expectedDestinationPort, testDepositorProperties.getDestinationPort());
      assertEquals("Incorrect destinationEndpoint", expectedDestinationEndpoint,
            testDepositorProperties.getDestinationEndpoint());
      assertEquals("Incorrect groupId", expectedGroupId, testDepositorProperties.getGroupId());
      assertNotNull("No environment", testDepositorProperties.getEnvironment());
   }

   @Test
   public void noValuesSet() {

      testDepositorProperties.initialize();

      assertNotNull(testDepositorProperties.getKafkaBrokers());
      assertNotNull(testDepositorProperties.getSubscriptionTopic());
      assertNotNull(testDepositorProperties.getDestinationProtocol());
      assertNotNull(testDepositorProperties.getDestinationUrl());
      assertNotNull(testDepositorProperties.getDestinationPort());
      assertNotNull(testDepositorProperties.getDestinationEndpoint());
      assertNotNull(testDepositorProperties.getGroupId());
   }

}
