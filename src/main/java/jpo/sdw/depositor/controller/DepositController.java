package jpo.sdw.depositor.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor;
import jpo.sdw.depositor.depositors.SDWDepositor;

@Component
public class DepositController {

   private static final Logger logger = LoggerFactory.getLogger(DepositController.class);

   private KafkaConsumerRestDepositor kafkaConsumerRestDepositor;

   private DepositorProperties depositorProperties;

   @Autowired
   public DepositController(DepositorProperties depositorProperties) throws URISyntaxException {

      List<ClientHttpRequestInterceptor> authHeaders = new ArrayList<ClientHttpRequestInterceptor>();
      authHeaders.add(
            new BasicAuthorizationInterceptor(depositorProperties.getUsername(), depositorProperties.getPassword()));

      RestTemplate basicAuthRestTemplate = new RestTemplate();
      basicAuthRestTemplate.setInterceptors(authHeaders);
      
      SDWDepositor sdwDepositor = new SDWDepositor(basicAuthRestTemplate,
            new URI(depositorProperties.getDestinationUrl()));

      this.kafkaConsumerRestDepositor = new KafkaConsumerRestDepositor(
            KafkaConsumerFactory.createConsumer(depositorProperties), sdwDepositor,
            depositorProperties.getEncodeType());
      
      this.depositorProperties = depositorProperties;
   }

   @PostConstruct
   public void run() {
      logger.info("Starting KafkaConsumerRestDepositor listening to topic(s): <{}> and forwarding to SDW at URL: <{}>",
            depositorProperties.getSubscriptionTopics(), depositorProperties.getDestinationUrl());

      kafkaConsumerRestDepositor.run(depositorProperties.getSubscriptionTopics());
   }

}