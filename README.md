# SmartBook

## Une plateforme de lecture de livres avec système de recommandations intelligentes

SmartBook est une application web Java qui offre une plateforme de lecture de livres en ligne avec des recommandations intelligentes. L'application utilise le filtrage collaboratif pour suggérer des livres aux utilisateurs en fonction de leurs habitudes de lecture et de leurs évaluations.

## Fonctionnalités

- **Gestion des utilisateurs**: Inscription, connexion et profils utilisateurs
- **Bibliothèque de livres**: Parcourez et recherchez dans une collection de livres
- **Suivi de progression de lecture**: Suivez votre progression pour chaque livre
- **Système d'évaluation**: Notez les livres et rédigez des critiques
- **Recommandations intelligentes**: Recevez des recommandations personnalisées basées sur vos préférences et votre historique de lecture
- **Interface réactive**: Interface moderne et responsive construite avec Bootstrap

## Stack technique

- **Backend**: [Spring Boot](https://spring.io/projects/spring-boot)
- **Sécurité**: [Spring Security](https://spring.io/projects/spring-security)
- **Base de données**: [MySQL](https://www.mysql.com/)
- **ORM**: [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- **Frontend**: [Thymeleaf](https://www.thymeleaf.org/), [Bootstrap](https://getbootstrap.com/)
- **Outil de build**: [Maven](https://maven.apache.org/)

## Instructions d'installation

### Prérequis

- Java 11 ou supérieur
- MySQL 8.0 ou supérieur
- Maven 3.6 ou supérieur

### Configuration de la base de données

1. Créez une base de données MySQL nommée `smartbook`
2. Mettez à jour les identifiants de la base de données dans `application.properties` si nécessaire

### Construction et exécution

1. Clonez le dépôt
   ```bash
   git clone https://github.com/votre-nom/smartbook.git
   cd smartbook
   ```

2. Construisez l'application
   ```bash
   mvn clean package
   ```

3. Exécutez l'application
   ```bash
   java -jar target/smartbook-0.0.1-SNAPSHOT.jar
   ```

4. Accédez à l'application à l'adresse http://localhost:8080

### Comptes par défaut

L'application initialise certaines données lorsqu'elle s'exécute avec le profil `dev` :

- **Administrateur**:
  - Nom d'utilisateur: `admin`
  - Mot de passe: `admin123`
  
- **Utilisateurs réguliers**:
  - Nom d'utilisateur: `user1`
  - Mot de passe: `password`

## Système de recommandation

Le système de recommandation utilise le filtrage collaboratif avec le coefficient de corrélation de Pearson pour trouver des utilisateurs similaires et recommander des livres qu'ils ont appréciés. Si les données disponibles ne sont pas suffisantes pour des recommandations pertinentes, le système propose alors les livres les plus populaires.

## Développement

Pour exécuter en mode développement avec des données d'exemple :

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Cette commande initialisera la base de données avec des exemples de livres, d'utilisateurs et d'évaluations à des fins de test.