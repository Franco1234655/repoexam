package com.example.demo.queue;

import com.example.demo.PojaGenerated;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/** File d'attente utilisée pour déclencher le traitement asynchrone (niveau de gris + email). */
@PojaGenerated
@Configuration
public class QueueConf {

  @Getter private final String queueUrl;
  private final Region region;

  public QueueConf(
      @Value("${aws.sqs.image-processing-queue-url}") String queueUrl,
      @Value("eu-west-3") Region region) {
    this.queueUrl = queueUrl;
    this.region = region;
  }

  @Bean
  public SqsClient getSqsClient() {
    return SqsClient.builder().region(region).build();
  }
}
