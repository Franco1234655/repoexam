package com.example.demo.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@AllArgsConstructor
public class ImageUploadEventPublisher {

  private final QueueConf queueConf;
  // Le bean ObjectMapper @Primary est déjà défini dans EndpointConf (existant dans ton repo).
  private final ObjectMapper objectMapper;

  @SneakyThrows
  public void publish(ImageProcessingMessage message) {
    String body = objectMapper.writeValueAsString(message);
    queueConf
        .getSqsClient()
        .sendMessage(
            SendMessageRequest.builder().queueUrl(queueConf.getQueueUrl()).messageBody(body).build());
  }
}