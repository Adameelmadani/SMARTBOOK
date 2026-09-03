# SmartBook

[![Java](https://img.shields.io/badge/Java-11+-blue.svg)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Une plateforme de lecture de livres en ligne avec système de recommandations intelligentes basé sur le filtrage collaboratif.

<img src="demo/demo.gif">

## Fonctionnalités

- **Gestion des utilisateurs** : Inscription, connexion, profils utilisateurs avec rôles (Admin/User)
- **Bibliothèque de livres** : Parcourez, recherchez et filtrez une collection de livres
- **Lecture en ligne** : Visualisez les livres directement dans le navigateur (PDF.js)
- **Suivi de progression** : Suivez votre progression de lecture page par page
- **Système d'évaluation** : Notez les livres (1-5 étoiles) et rédigez des critiques
- **Recommandations intelligentes** : Recommandations personnalisées basées sur le filtrage collaboratif (corrélation de Pearson)
- **Interface responsive** : Interface moderne avec Bootstrap 5 et Thymeleaf
- **Sécurité** : Authentification Spring Security avec BCrypt, protection CSRF, gestion des rôles

## Stack Technique

| Composant | Technologie |
|-----------|-------------|
| **Backend** | Spring Boot 2.7.0 |
| **Sécurité** | Spring Security 5 + BCrypt |
| **Base de données** | H2 (dev) / MySQL (prod) |
| **ORM** | Spring Data JPA / Hibernate |
| **Frontend** | Thymeleaf + Bootstrap 5 |
| **Build** | Maven 3.6+ |
| **Java** | 11+ |
| **Lecture PDF** | PDF.js (via CDN) |
| **Recommandations** | Filtrage collaboratif (Pearson Correlation) |

## Architecture

```
src/main/java/com/smartbook/
├── config/           # Configuration (DataSeeder, DatabaseInitializer)
├── controller/       # Contrôleurs MVC (Auth, Book, BookFile, Home)
├── model/            # Entités JPA (Book, User, Rating, BookProgress, Role)
├── repository/       # Repositories Spring Data JPA
├── security/         # Config sécurité, UserDetails
├── service/          # Services métier (RecommendationService, UserDetailsService)
└── SmartBookApplication.java
```

## Prérequis

- **Java 11+** (testé avec Java 11 et 17)
- **Maven 3.6+**
- **Git** (pour cloner le repo)

## Installation et Démarrage

### 1. Cloner le repository

```bash
git clone https://github.com/Adameelmadani/smartbook.git
cd smartbook
```

### 2. Construire le projet

```bash
mvn clean package
```

### 3. Exécuter l'application

**Mode production (JAR) :**
```bash
java -jar target/smartbook-0.0.1-SNAPSHOT.jar
```

**Mode développement (avec données de test) :**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Accéder à l'application

Ouvrez votre navigateur à : **http://localhost:8080**

## Comptes par défaut (profil `dev`)

| Rôle | Username | Mot de passe |
|------|----------|--------------|
| Admin | `admin` | `admin123` |
| Utilisateur | `user1` | `password` |
| Utilisateur | `user2` | `password` |

> **Note** : Ces comptes sont créés automatiquement au démarrage avec le profil `dev` via `DataSeeder`.

## Configuration

### Base de données (application.properties)

```properties
# Développement (H2 en mémoire)
spring.datasource.url=jdbc:h2:mem:smartbook
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Production (MySQL) - décommentez et configurez
# spring.datasource.url=jdbc:mysql://localhost:3306/smartbook?useSSL=false&serverTimezone=UTC
# spring.datasource.username=your_username
# spring.datasource.password=your_password
# spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### Profils Maven

```bash
# Développement (H2 + données de test)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production (MySQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Système de Recommandation

Le système utilise le **filtrage collaboratif basé sur les utilisateurs** avec la **corrélation de Pearson** :

1. **Calcul de similarité** : Coefficient de corrélation de Pearson entre utilisateurs basé sur les notes communes
2. **Sélection des voisins** : Top-K utilisateurs les plus similaires (seuil de similarité configurable)
3. **Prédiction** : Note prédite = moyenne utilisateur + somme pondérée des écarts des voisins
4. **Fallback** : Si données insuffisantes → recommandations populaires (livres les mieux notés)

**Configuration** (`RecommendationService.java`) :
```java
private static final int MIN_COMMON_RATINGS = 3;  // Notes communes minimales
private static final double SIMILARITY_THRESHOLD = 0.1;  // Seuil similarité
private static final int MAX_NEIGHBORS = 10;  // Voisins max
```

## Structure de la Base de Données

### Entités Principales

| Entité | Description |
|--------|-------------|
| `User` | Utilisateurs (username, email, password, roles) |
| `Role` | Rôles (ROLE_USER, ROLE_ADMIN) |
| `Book` | Livres (titre, auteur, description, couverture, fichier PDF) |
| `Rating` | Notes (user, book, score 1-5, commentaire) |
| `BookProgress` | Progression lecture (user, book, page actuelle, total pages) |

### Relations

- `User` ↔ `Role` : Many-to-Many
- `User` ↔ `Rating` : One-to-Many
- `User` ↔ `BookProgress` : One-to-Many
- `Book` ↔ `Rating` : One-to-Many
- `Book` ↔ `BookProgress` : One-to-Many

## API Endpoints

### Authentification
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/signin` | Page de connexion |
| GET | `/signup` | Page d'inscription |
| POST | `/signin` | Traiter connexion |
| POST | `/signup` | Traiter inscription |
| GET | `/logout` | Déconnexion |

### Livres
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/` | Page d'accueil (livres récents) |
| GET | `/discover` | Découvrir tous les livres (recherche, filtres) |
| GET | `/books/{id}` | Détail d'un livre |
| GET | `/books/{id}/read` | Lecteur PDF (lecture en ligne) |
| GET | `/books/{id}/download` | Télécharger le PDF |

### Profil & Progression
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/profile` | Profil utilisateur |
| GET | `/profile/progress` | Progression de lecture |
| POST | `/books/{id}/progress` | Mettre à jour progression |
| POST | `/books/{id}/rate` | Noter un livre |

### Administration
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/admin/dashboard` | Tableau de bord admin |
| GET | `/admin/books` | Gestion livres |
| POST | `/admin/books` | Ajouter livre |
| POST | `/admin/books/{id}/upload` | Uploader PDF |

## Captures d'écran

| Page | Description |
|------|-------------|
| Accueil | Page d'accueil avec livres recommandés |
| Découvrir | Bibliothèque avec filtres et recherche |
| Lecture | Lecteur PDF intégré avec progression |
| Profil | Profil utilisateur et statistiques |

## Développement

### Structure du Projet

```
smartbook/
├── src/
│   ├── main/
│   │   ├── java/com/smartbook/     # Code source Java
│   │   └── resources/
│   │       ├── static/             # CSS, JS, images
│   │       ├── templates/          # Templates Thymeleaf
│   │       └── application.properties
│   └── test/                       # Tests unitaires
├── target/                         # Artefacts de build (ignorés par git)
├── uploads/                        # Fichiers uploadés (ignorés par git)
├── data/                           # Base H2 locale (ignorée par git)
├── pom.xml                         # Configuration Maven
└── README.md
```

### Lancer les tests

```bash
mvn test
```

### Build pour production

```bash
mvn clean package -Pprod
```

Le JAR sera dans `target/smartbook-0.0.1-SNAPSHOT.jar`

## Déploiement

### Docker (optionnel)

```dockerfile
FROM openjdk:11-jre-slim
COPY target/smartbook-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t smartbook .
docker run -p 8080:8080 smartbook
```

### Variables d'environnement (Production)

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/smartbook
export SPRING_DATASOURCE_USERNAME=smartbook
export SPRING_DATASOURCE_PASSWORD=secret
export SERVER_PORT=8080
```

## Licence

Ce projet est sous licence **MIT** - voir le fichier [LICENSE](LICENSE) pour plus de détails.