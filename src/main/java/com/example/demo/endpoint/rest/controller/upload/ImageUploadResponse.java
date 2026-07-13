package com.example.demo.endpoint.rest.controller.upload;

import com.example.demo.upload.ImageUpload;
import java.time.Instant;
import java.util.UUID;

public record ImageUploadResponse(UUID id, String nomFichier, String email, Instant createdAt) {
    public static ImageUploadResponse from(ImageUpload entity) {
        return new ImageUploadResponse(
                entity.getId(), entity.getNomFichier(), entity.getEmail(), entity.getCreatedAt());
    }
}