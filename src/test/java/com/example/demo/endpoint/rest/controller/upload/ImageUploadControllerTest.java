package com.example.demo.endpoint.rest.controller.upload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.upload.ImageUpload;
import com.example.demo.upload.ImageUploadService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageUploadController.class)
class ImageUploadControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ImageUploadService imageUploadService;

  @Test
  void post_returns201_withSavedMetadata() throws Exception {
    var id = UUID.randomUUID();
    var saved =
        new ImageUpload(id, "photo.jpg", "eleve@hei.school", Instant.parse("2026-07-13T10:00:00Z"));
    when(imageUploadService.upload(any(), eq("eleve@hei.school"))).thenReturn(saved);

    var file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "contenu-bidon".getBytes());

    mockMvc
        .perform(multipart("/images").file(file).param("email", "eleve@hei.school"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nomFichier").value("photo.jpg"))
        .andExpect(jsonPath("$.email").value("eleve@hei.school"));
  }

  @Test
  void get_byId_returnsMetadata() throws Exception {
    var id = UUID.randomUUID();
    var saved = new ImageUpload(id, "photo.png", "eleve@hei.school", Instant.now());
    when(imageUploadService.getById(id)).thenReturn(saved);

    mockMvc
        .perform(get("/images/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nomFichier").value("photo.png"));
  }

  @Test
  void get_list_returnsAll() throws Exception {
    when(imageUploadService.getAll()).thenReturn(List.of());

    mockMvc.perform(get("/images")).andExpect(status().isOk());
  }
}
