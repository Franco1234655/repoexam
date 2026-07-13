package com.example.demo.upload;

import com.example.demo.bucket.BucketConf;
import com.example.demo.file.zip.FileTyper;
import com.example.demo.queue.ImageProcessingMessage;
import com.example.demo.queue.ImageUploadEventPublisher;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@AllArgsConstructor
public class ImageUploadService {

    private static final Set<MediaType> ALLOWED_TYPES = Set.of(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG);
    private static final String ORIGINAL_PREFIX = "original/";

    private final ImageUploadRepository repository;
    private final FileTyper fileTyper;
    private final BucketConf bucketConf;
    private final ImageUploadEventPublisher eventPublisher;

    /** Partie SYNCHRONE: validation + upload S3 de l'original + écriture en base. */
    public ImageUpload upload(MultipartFile file, String email) {
        File tempFile = toTempFile(file);
        try {
            MediaType detectedType = fileTyper.apply(tempFile);
            if (!ALLOWED_TYPES.contains(detectedType)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Seuls les formats image/jpeg et image/png sont acceptés");
            }

            UUID id = UUID.randomUUID();
            String originalKey = ORIGINAL_PREFIX + id + "-" + file.getOriginalFilename();

            bucketConf
                    .getS3Client()
                    .putObject(
                            PutObjectRequest.builder()
                                    .bucket(bucketConf.getBucketName())
                                    .key(originalKey)
                                    .contentType(detectedType.toString())
                                    .build(),
                            RequestBody.fromFile(tempFile));

            var imageUpload = new ImageUpload(id, file.getOriginalFilename(), email, Instant.now());
            repository.save(imageUpload);

            // Déclenche la partie ASYNCHRONE (grayscale + email), traitée par un autre handler Lambda.
            eventPublisher.publish(new ImageProcessingMessage(id, originalKey, file.getOriginalFilename(), email));

            return imageUpload;
        } finally {
            tempFile.delete();
        }
    }

    public ImageUpload getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<ImageUpload> getAll() {
        return repository.findAll();
    }

    private File toTempFile(MultipartFile file) {
        try {
            File tempFile = File.createTempFile("upload-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}