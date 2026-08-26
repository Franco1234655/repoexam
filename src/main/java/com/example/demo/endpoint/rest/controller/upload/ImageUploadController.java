package com.example.demo.endpoint.rest.controller.upload;

import com.example.demo.upload.ImageUploadService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class ImageUploadController {

  private final ImageUploadService imageUploadService;

  /** POST /images (multipart/form-data: file, email) -- synchrone. */
  @PostMapping(value = "/images", consumes = "multipart/form-data")
  public ResponseEntity<ImageUploadResponse> uploadImage(
      @RequestParam("file") MultipartFile file, @RequestParam("email") String email) {
    var saved = imageUploadService.upload(file, email);
    return ResponseEntity.status(HttpStatus.CREATED).body(ImageUploadResponse.from(saved));
  }

  /** GET /images -- liste toutes les images enregistrées. */
  @GetMapping("/images")
  public List<ImageUploadResponse> listImages() {
    return imageUploadService.getAll().stream().map(ImageUploadResponse::from).toList();
  }

  /** GET /images/{id} -- une image par id. */
  @GetMapping("/images/{id}")
  public ImageUploadResponse getImage(@PathVariable UUID id) {
    return ImageUploadResponse.from(imageUploadService.getById(id));
  }
}
