package jpo.sdw.depositor.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor;
import jpo.sdw.depositor.depositors.SDWDepositor;

@Controller
public class DepositController {

   @Autowired
   public DepositController(DepositorProperties depositorProperties) throws URISyntaxException {
      KafkaConsumer<String, String> kafkaConsumer = KafkaConsumerFactory
            .createConsumer(depositorProperties.getKafkaBrokers());

      URI destUri = assembleDestinationUri(depositorProperties);

      SDWDepositor sdwDepositor = new SDWDepositor(new RestTemplate(), destUri);
      KafkaConsumerRestDepositor kcrd = new KafkaConsumerRestDepositor(kafkaConsumer, sdwDepositor);

      kcrd.run(depositorProperties.getSubscriptionTopic());
   }

   private URI assembleDestinationUri(DepositorProperties dp) throws URISyntaxException {
      return new URI(String.format("%s//%s:%s/%s", dp.getDestinationProtocol(), dp.getDestinationUrl(),
            dp.getDestinationPort(), dp.getDestinationEndpoint()));
   }

}