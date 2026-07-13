package com.example.demo.queue;

import java.util.UUID;

/**
 * Ce que le POST envoie à la queue: assez d'infos pour que le worker (dans une autre invocation
 * Lambda) retrouve l'image d'origine sur S3 -- on n'envoie JAMAIS un chemin de fichier local
 * temporaire, il n'existera plus dans l'invocation asynchrone.
 */
public record ImageProcessingMessage(
        UUID id, String originalS3Key, String nomFichier, String email) {}