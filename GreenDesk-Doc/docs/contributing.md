# Guide de contribution

Merci de vouloir contribuer à GreenDesk ! Guide pour contribuer au projet.

## Avant de commencer

## Avant de commencer

- Familiarisez-vous avec le [Guide de démarrage](getting-started.md)
- Lisez l'[Architecture](architecture.md)
- Consultez le [Guide des tests](testing.md)
- Vérifiez les [Problèmes ouverts](https://github.com/yourteam/greendesk/issues)

## Installation pour développement

### 1. Fork et clone

```bash
# Forker sur GitHub
# Puis cloner votre fork

git clone https://github.com/votre-pseudo/greendesk.git
cd greendesk

# Ajouter upstream
git remote add upstream https://github.com/originalteam/greendesk.git
```
// Ne pas faire
This code is bad.

// À faire
Consider using a more descriptive variable name here, as `x` doesn't 
indicate what value it represents. Something like `plantHealthFactor` 
would be clearer.

### 2. Créer branche feature

```bash
### Comportement

- Être respectueux et constructif
- Accepter les critiques
- Aider autres contributeurs
- Attribuer crédits
# Créer branche
git checkout -b feature/mon-feature
# ou
### Idées issues populaires

- Interface web améliorée
- Meilleure visualisation données
- Système d'authentification
- Optimisations performance
- API mobile
- Support multilingue
# Configurer Python env
configure_python_environment

# Ou simplement :
./gradlew build
```

## Développement

### Structure du code

```
src/main/java/org/example/
├── controllers/      # Endpoints REST
├── services/        # Logique métier
├── repositories/    # Accès données
├── entities/        # Models
└── GreenDesk.java  # Main application
```

### Conventions de code

#### Noms

```java
// Classes : PascalCase
public class SpeciesService { }

// Méthodes : camelCase
public void createSpecies() { }

// Constantes : UPPER_SNAKE
public static final String DEFAULT_SPECIES = "Rose";
```

#### Format

```java
// Utiliser Lombok
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Species { }

// Valider entrées
@NotBlank(message = "Name is required")
private String name;

// Logs appropriés
logger.info("Creating species: {}", name);
```

#### Annotations Spring

```java
// Utiliser @Autowired ou constructeur
@Service
public class SpeciesService {
    private final SpeciesRepository repository;
    
    public SpeciesService(SpeciesRepository repository) {
        this.repository = repository;
    }
}
```
// Mapper les endpoints
@GetMapping("/{name}")
public ResponseEntity<Species> getByName(@PathVariable String name) {
    return speciesRepository.findByName(name)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

### Écrire des tests

Tout code nouveau doit avoir des tests !

```java
@SpringBootTest
public class MyNewFeatureTest {
    
    @Autowired
    private MyNewFeatureService service;
    
    @Test
    public void shouldDoSomething() {
        // Arrange
        MyEntity entity = new MyEntity(...);
        
        // Act
        MyResult result = service.doSomething(entity);
        
        // Assert
        assertNotNull(result);
        assertEquals(expected, result.getValue());
    }
    
    @Test
    public void shouldThrowErrorOnInvalidInput() {
        // Arrange
        MyEntity invalid = new MyEntity(null);
        
        // Act & Assert
        # Guide de contribution

        Merci pour votre intérêt à contribuer à GreenDesk. Ce document explique les étapes recommandées pour proposer des modifications, soumettre des correctifs et contribuer de façon productive.

        ## Avant de commencer

        - Lisez le [Guide de démarrage](getting-started.md).
        - Parcourez l'[architecture du projet](architecture.md) pour comprendre la structure.
        - Consultez le [Guide des tests](testing.md) et les tests existants.
        - Vérifiez les issues ouvertes et les labels pertinents sur GitHub.

        ## Installation et configuration locale

        1. Forkez le dépôt puis clonez votre fork :

        ```bash
        git clone https://github.com/votre-pseudo/greendesk.git
        cd greendesk
        git remote add upstream https://github.com/originalteam/greendesk.git
        ```

        2. Créez une branche pour votre travail :

        ```bash
        git fetch upstream
        git checkout -b feature/ma-fonctionnalite
        ```

        3. Construisez et exécutez les tests localement :

        ```bash
        ./gradlew build
        ./gradlew test
        ```

        ## Conventions et bonnes pratiques

        Respectez les conventions de nommage et de style du projet :

        - Classes : `PascalCase`
        - Méthodes : `camelCase`
        - Constantes : `UPPER_SNAKE`

        Utilisez Lombok là où il est déjà utilisé, validez les entrées et ajoutez des logs pertinents.

        Exemple :

        ```java
        // Classes : PascalCase
        public class SpeciesService { }

        // Méthodes : camelCase
        public void createSpecies() { }

        // Constantes : UPPER_SNAKE
        public static final String DEFAULT_SPECIES = "Rose";
        ```

        ### Tests

        Tout nouveau code doit être accompagné de tests unitaires ou d'intégration selon le besoin. Exécutez `./gradlew test` et assurez-vous que les tests passent avant de créer la pull request.

        ## Checklist avant commit

        - Le code compile sans erreurs
        - Les tests passent (`./gradlew test`)
        - La couverture de test est suffisante pour les nouvelles fonctionnalités
        - Les messages de commit sont clairs et descriptifs
        - La documentation est mise à jour si nécessaire

        ## Soumettre une Pull Request

        1. Poussez votre branche vers votre fork :

        ```bash
        git push origin feature/ma-fonctionnalite
        ```

        2. Créez une Pull Request sur GitHub en choisissant la branche cible appropriée. Remplissez le template PR en expliquant clairement les changements et comment les tester.

        ### Modèle de description pour la PR

        ```
        ## Description
        Résumé des modifications et motivations.

        ## Type de changement
        - Bug fix
        - Nouvelle fonctionnalité
        - Documentation

        ## Comment tester
        Étapes pour reproduire et valider les changements.
        ```

        ## Processus de revue et fusion

        - Attendre les retours des reviewers et adresser les commentaires.
        - Les tests CI doivent être verts avant de fusionner.
        - Après approbation, fusionner selon la stratégie du projet (merge/squash/rebase).

        ## Après fusion

        ```bash
        git checkout main
        git fetch upstream
        git rebase upstream/main
        git push origin main
        git branch -d feature/ma-fonctionnalite
        ```

        Merci pour votre contribution !
## Bonus contributions

### Idées issues populaires

- 🎨 Interface web améliorée
- Meilleure visualisation données
- Système d'authentification
- ⚡ Optimisations performance
- 📱 API mobile
- 🌐 Support multilingue

### Documentation

- Ajouter tutoriels
- Améliorer exemples
- Traduire en autres langues
- Créer vidéos tutoriels

## Règles importantes

### Comportement

- Être respectueux et constructif
- Accepter les critiques
- Aider autres contributeurs
- Attribuer crédits

### Licensing

Tout code contribué doit être compatible avec la licence du projet.

## Questions ?

- 📧 Email : contact@greendesk.dev
- 💬 Discord : [Lien]
- Issues : Poser des questions en créant une issue avec label `question`

## Processus de review et merge

1. **Créer PR** - Suivre template
2. **Tests CI** - Vérifier que tests passent
3. **Code review** - Attendez review d'au moins 2 mainteneurs
4. **Approbation** - PR approuvée
5. **Merge** - Fusionner dans main

## After merge

```bash
# Sync local repo
git checkout main
git fetch upstream
git rebase upstream/main
git push origin main

# Supprimer branche locale
git branch -d feature/mon-feature
```

---

**Merci de contribuer !** 🌱

Votre contribution rend GreenDesk meilleur pour tous. Bienvenue dans l'équipe !
