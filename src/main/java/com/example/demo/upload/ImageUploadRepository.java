package com.example.demo.upload;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageUploadRepository extends JpaRepository<ImageUpload, UUID> {}
