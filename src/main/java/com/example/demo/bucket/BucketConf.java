package com.example.demo.bucket;

import com.example.demo.PojaGenerated;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * NB: nécessite d'ajouter 'software.amazon.awssdk:s3' à build.gradle (pas encore présent dans le
 * template). Convention alignée sur EmailConf: le bucket est lu depuis la propriété "aws.s3.bucket"
 * (mappée en test via src/test/java/.../conf/BucketConf#configureProperties, déjà présent dans ton
 * repo).
 */
@PojaGenerated
@Configuration
public class BucketConf {

  @Getter private final String bucketName;
  private final Region region;

  public BucketConf(
      @Value("${aws.s3.bucket}") String bucketName, @Value("eu-west-3") Region region) {
    this.bucketName = bucketName;
    this.region = region;
  }

  @Bean
  public S3Client getS3Client() {
    return S3Client.builder().region(region).build();
  }
}
