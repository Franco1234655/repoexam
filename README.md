# poja-starter-template

# Upload d'image + traitement asynchrone (POJA) — récapitulatif complet

## Fonctionnement

**POST /images** (multipart/form-data: `file`, `email`) — synchrone :
1. Détecte le vrai type du fichier avec `FileTyper` (Apache Tika).
2. Rejette (400) si ce n'est ni `image/jpeg` ni `image/png`.
3. Upload l'original sur S3 (`original/{id}-{nomFichier}`).
4. Insère une ligne dans `image_upload` (`id` UUID | `nom_fichier` | `email` | `created_at`).
5. Publie un message SQS (`ImageProcessingMessage`) pour déclencher la suite en asynchrone.
6. Répond `201` avec les métadonnées enregistrées.

**GET /images** et **GET /images/{id}** — lecture des métadonnées enregistrées, synchrone.

**AsyncImageHandler** (déclenché par SQS, invocation Lambda séparée) :
1. Télécharge l'original depuis S3.
2. Convertit en niveaux de gris (`GrayscaleConverter`).
3. Réuploade le résultat sur S3 (`grayscale/{id}-{nomFichier}`).
4. Envoie l'email via `Mailer` (déjà existant dans le repo) avec l'image en pièce jointe.


Fichiers existants dans le repo, non modifiés : `PojaApplication.java`, `PojaGenerated.java`, `EndpointConf.java`,
`RequestLoggerConfigurer.java`, `LambdaHandler.java`, `mail/*`, `file/hash/*`, `file/zip/FileTyper.java`,
`endpoint/rest/controller/health/*`, `concurrency/*`, `datastructure/*`.

## Dépendances ajoutées à `build.gradle`

```gradle
implementation 'software.amazon.awssdk:s3:2.21.40'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa:3.2.2'
runtimeOnly 'org.postgresql:postgresql:42.7.3'
implementation 'org.springframework.boot:spring-boot-starter-validation:3.2.2'
implementation 'org.flywaydb:flyway-core:10.10.0'
implementation 'org.flywaydb:flyway-database-postgresql:10.10.0'
```

## Variables d'environnement requises

Définies dans `.env.local` (jamais commité) pour le local, et côté dashboard console.poja.io /
secrets GitHub Actions pour le déploiement preprod :

| Variable | Rôle |
|---|---|
| `DB_URL` | URL JDBC Neon Postgres |
| `DB_USERNAME` | utilisateur Neon |
| `DB_PASSWORD` | mot de passe Neon |
| `AWS_S3_BUCKET` | nom du bucket S3 (originaux + versions niveaux de gris) |
| `AWS_SQS_IMAGE_PROCESSING_QUEUE_URL` | URL de la queue SQS qui déclenche `AsyncImageHandler` |

Charger le fichier local avant de lancer l'app :
```bash
export $(cat .env.local | grep -v '^#' | xargs)
./gradlew bootRun
```

## Base de données

Migration Flyway (`V1__create_image_upload_table.sql`) au lieu d'un `ddl-auto=update` :
```sql
CREATE TABLE image_upload (
    id UUID PRIMARY KEY,
    nom_fichier VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```
`spring.jpa.hibernate.ddl-auto=validate` : Hibernate vérifie juste que l'entité correspond au
schéma, il ne le modifie jamais tout seul.

## Tests

```bash
./gradlew test
```

- `GrayscaleConverterTest` : vérifie qu'une image colorée ressort en niveaux de gris (R=G=B).
  Aucun mock, aucun réseau.
- `ImageUploadServiceTest` : POST accepté pour un jpeg (DB + SQS mockées), rejeté (400) pour un
  PDF. Aucune base réelle nécessaire.
- `ImageUploadControllerTest` : vérifie les routes HTTP (`POST /images`, `GET /images`,
  `GET /images/{id}`) via `MockMvc`, sans lancer le serveur.

Aucun de ces tests n'a besoin de `.env.local` chargé ni d'accès réseau — ils tournent aussi en CI.

## Sécurité — points vérifiés

- Le mot de passe Neon original partagé dans la conversation a été/doit être régénéré côté Neon.
- `.env.local` est dans `.gitignore`, jamais commité.
- `application.properties` ne contient que des `${VARIABLE}`, aucune valeur en clair.

## Ce qui reste à faire / vérifier

1. Créer le vrai bucket S3 et la vraie queue SQS sur AWS s'ils n'existent pas encore.
2. Définir les 5 variables d'environnement côté déploiement preprod (dashboard console.poja.io
   ou secrets GitHub Actions selon ce qu'utilise ton cours — non vérifié avec certitude de mon
   côté).
3. Vérifier après le déploiement que `GET /ping` puis `POST /images` répondent correctement sur
   l'URL Lambda preprod.
4. Ce code n'a pas été compilé/exécuté dans mon environnement (pas d'accès Maven Central) —
   valide-le avec `./gradlew build` avant de pousser en `prod`.
