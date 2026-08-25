package com.example.demo.upload;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Table cible : id (UUID) | nom_fichier | email | created_at
 *
 * <p>NB: nécessite d'ajouter spring-boot-starter-data-jpa + un driver (postgresql) à build.gradle
 * -- voir build-gradle-additions.txt.
 */
@Entity
@Table(name = "image_upload")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageUpload {

  @Id private UUID id;

  @Column(name = "nom_fichier", nullable = false)
  private String nomFichier;

  @Column(nullable = false)
  private String email;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}