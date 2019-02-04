package jpo.sdw.depositor.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor;
import jpo.sdw.depositor.depositors.SDWDepositor;

@Controller
public class DepositController {

   private static final Logger logger = LoggerFactory.getLogger(DepositController.class);

   @Autowired
   public DepositController(DepositorProperties depositorProperties) throws URISyntaxException {

      List<ClientHttpRequestInterceptor> authHeaders = new ArrayList<ClientHttpRequestInterceptor>();
      authHeaders.add(
            new BasicAuthorizationInterceptor(depositorProperties.getUsername(), depositorProperties.getPassword()));

      RestTemplate basicAuthRestTemplate = new RestTemplate();
      basicAuthRestTemplate.setInterceptors(authHeaders);

      SDWDepositor sdwDepositor = new SDWDepositor(basicAuthRestTemplate,
            new URI(depositorProperties.getDestinationUrl()));

      KafkaConsumerRestDepositor kcrd = new KafkaConsumerRestDepositor(
            KafkaConsumerFactory.createConsumer(depositorProperties), sdwDepositor,
            depositorProperties.getEncodeType());

      logger.info("Starting KafkaConsumerRestDepositor listening to topic(s): <{}> and forwarding to SDW at URL: <{}>",
            depositorProperties.getSubscriptionTopics(), depositorProperties.getDestinationUrl());
      kcrd.run(depositorProperties.getSubscriptionTopics());
   }

}