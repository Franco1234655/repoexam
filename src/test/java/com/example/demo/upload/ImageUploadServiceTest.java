package com.example.demo.upload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.demo.bucket.BucketConf;
import com.example.demo.file.zip.FileTyper;
import com.example.demo.queue.ImageUploadEventPublisher;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class ImageUploadServiceTest {

    private final ImageUploadRepository repository = mock(ImageUploadRepository.class);
    private final FileTyper fileTyper = mock(FileTyper.class);
    private final BucketConf bucketConf = mock(BucketConf.class);
    private final ImageUploadEventPublisher eventPublisher = mock(ImageUploadEventPublisher.class);
    private final S3Client s3Client = mock(S3Client.class);

    private final ImageUploadService service =
            new ImageUploadService(repository, fileTyper, bucketConf, eventPublisher);

    @Test
    void upload_savesAndPublishesEvent_whenJpeg() {
        var file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "contenu-bidon".getBytes());
        when(fileTyper.apply(any(File.class))).thenReturn(MediaType.IMAGE_JPEG);
        when(bucketConf.getS3Client()).thenReturn(s3Client);
        when(bucketConf.getBucketName()).thenReturn("test-bucket");
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        var result = service.upload(file, "eleve@hei.school");

        assertThat(result.getNomFichier()).isEqualTo("photo.jpg");
        assertThat(result.getEmail()).isEqualTo("eleve@hei.school");
        verify(repository).save(result);
        verify(eventPublisher).publish(any());
    }

    @Test
    void upload_rejectsUnsupportedFormat() {
        var file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "contenu-bidon".getBytes());
        when(fileTyper.apply(any(File.class))).thenReturn(MediaType.APPLICATION_PDF);

        assertThatThrownBy(() -> service.upload(file, "eleve@hei.school"))
                .isInstanceOf(ResponseStatusException.class);

        verifyNoInteractions(repository, eventPublisher);
    }
}