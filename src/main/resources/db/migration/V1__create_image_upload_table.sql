CREATE TABLE image_upload (
                              id UUID PRIMARY KEY,
                              nom_fichier VARCHAR(255) NOT NULL,
                              email VARCHAR(255) NOT NULL,
                              created_at TIMESTAMP NOT NULL
);