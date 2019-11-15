package jpo.sdw.depositor.controller;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import jpo.sdw.depositor.DepositorProperties;
import jpo.sdw.depositor.consumerdepositors.KafkaConsumerRestDepositor;
import jpo.sdw.depositor.depositors.SDWDepositor;

@Component
public class DepositController {

      private static final Logger logger = LoggerFactory.getLogger(DepositController.class);

      private KafkaConsumerRestDepositor kafkaConsumerRestDepositor;

      private DepositorProperties depositorProperties;

      @Autowired
      public DepositController(DepositorProperties depositorProperties, JavaMailSender mailSender)
                  throws URISyntaxException {
            WebClient client = WebClient.builder().baseUrl(depositorProperties.getDestinationUrl())
                        .defaultHeader("apikey", depositorProperties.getApiKey())
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();

            SDWDepositor sdwDepositor = new SDWDepositor(depositorProperties, mailSender, client,
                        new URI(depositorProperties.getDestinationUrl()));

            this.kafkaConsumerRestDepositor = new KafkaConsumerRestDepositor(
                        KafkaConsumerFactory.createConsumer(depositorProperties), sdwDepositor,
                        depositorProperties.getEncodeType());

            this.depositorProperties = depositorProperties;
      }

      @PostConstruct
      public void run() {
            logger.info("Starting KafkaConsumerRestDepositor listening to topic(s): <{}> and forwarding to SDX at URL: <{}>",
                        depositorProperties.getSubscriptionTopics(), depositorProperties.getDestinationUrl());

            kafkaConsumerRestDepositor.run(depositorProperties.getSubscriptionTopics());
      }

}