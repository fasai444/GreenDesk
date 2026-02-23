# Guide de contribution

Merci de vouloir contribuer à GreenDesk ! Guide pour contribuer au projet.

## Avant de commencer

- ✅ Familiarisez-vous avec le [Guide de démarrage](getting-started.md)
- ✅ Lisez l'[Architecture](architecture.md)
- ✅ Consultez le [Guide des tests](testing.md)
- ✅ Vérifiez les [Problèmes ouverts](https://github.com/yourteam/greendesk/issues)

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

### 2. Créer branche feature

```bash
# Mise à jour depuis upstream
git fetch upstream
git rebase upstream/main

# Créer branche
git checkout -b feature/mon-feature
# ou
git checkout -b fix/mon-bug
```

### 3. Configurer l'environnement

```bash
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
// ✅ Classes : PascalCase
public class SpeciesService { }

// ✅ Méthodes : camelCase
public void createSpecies() { }

// ✅ Constantes : UPPER_SNAKE
public static final String DEFAULT_SPECIES = "Rose";
```

#### Format

```java
// ✅ Utiliser Lombok
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Species { }

// ✅ Valider entrées
@NotBlank(message = "Name is required")
private String name;

// ✅ Logs appropriés
logger.info("Creating species: {}", name);
```

#### Annotations Spring

```java
// ✅ Utiliser @Autowired ou constructeur
@Service
public class SpeciesService {
    private final SpeciesRepository repository;
    
    public SpeciesService(SpeciesRepository repository) {
        this.repository = repository;
    }
}

// ✅ Mapper les endpoints
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
        assertThrows(IllegalArgumentException.class, () -> {
            service.doSomething(invalid);
        });
    }
}
```

### Lancer les tests localement

```bash
# Tous les tests
./gradlew test

# Tests spécifiques
./gradlew test --tests MyNewFeatureTest

# Avec rapport coverage
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Avant de committer

### ✅ Checklist

- [ ] Code compiles sans erreurs
- [ ] Tests passent : `./gradlew test`
- [ ] Coverage acceptable (> 80% pour nouvelles classes)
- [ ] Code suit conventions du projet
- [ ] Messages de commit clairs et descriptifs
- [ ] Pas de code inutilisé ou commented
- [ ] Documentation à jour si besoin

### Format des commits

```bash
git add .

# Format : type: description
# Types : feat, fix, docs, style, refactor, test, chore

git commit -m "feat: Add new species effect system"
git commit -m "fix: Correct plant health calculation"
git commit -m "docs: Update API documentation"
```

## Soumettre une Pull Request

### 1. Push votre branche

```bash
git push origin feature/mon-feature
```

### 2. Créer la PR

- Allez sur GitHub
- Cliquez "New Pull Request"
- Sélectionnez votre branche
- Remplissez le template

### Template PR

```markdown
## Description
Explication claire de vos changements.

## Type de changement
- [ ] Bug fix
- [ ] Nouvelle feature
- [ ] Breaking change
- [ ] Documentation update

## Comment tester ?
1. Créer une espèce
2. Créer une plante
3. Vérifier que l'effet s'applique correctement

## Checklist
- [ ] Tests ajoutés/modifiés
- [ ] Documentation mise à jour
- [ ] Pas de breaking changes
- [ ] Tests passent : `./gradlew test`
```

### 3. Répondre aux commentaires

Les reviewers peuvent demander des modifications. Répondez constructivement :

```bash
# Apporter les changements demandés
# Committer à la même branche
git add .
git commit -m "Address review comments"
git push origin feature/mon-feature

# La PR se met à jour automatiquement
```

## Types de contributions

### 🎯 Features

Nouvelles fonctionnalités :

```bash
git checkout -b feature/new-effect-type

# Implémenter nouvelle feature
# Tester
# PR

# Format commit : feat: Add [description]
```

### 🔨 Bug fixes

Corriger bugs :

```bash
git checkout -b fix/plant-health-bug

# Fixer le bug
# Ajouter test qui reproduit bug
# Vérifier test passe après fix

# Format commit : fix: [description]
```

### 📖 Documentation

Améliorer documentation :

```bash
git checkout -b docs/update-api-doc

# Mettre à jour .md files
# Pas besoin de tests

# Format commit : docs: [description]
```

### 🧹 Refactoring

Améliorer code existant :

```bash
git checkout -b refactor/improve-service-layer

# Refactoriser le code
# S'assurer tests passent
# Pas de changement comportement

# Format commit : refactor: [description]
```

## Étiquettes et problèmes

### Trouver bugs à corriger

Au lieu de créer nouvelles features, commencez par :

```bash
# Voir issues existantes
# Chercher labels : help-wanted, good-first-issue

# Commenter sur l'issue
# Proposer votre aide
```

### Créer une issue

Si vous trouvez un bug ou avez une idée :

```markdown
## Description du bug
Décrire le comportement inattendu.

## Étapes pour reproduire
1. Créer une espèce...
2. Créer une plante...
3. Vérifier l'état...

## Comportement attendu
Décrire ce qui devrait se passer.

## Logs d'erreur
Coller les stacktraces.

## Environnement
- OS: Windows/Mac/Linux
- Java version: 21
- MongoDB: 6.0
```

## Code review

### Être reviewer

- Consultez les PRs ouvertes
- Lisez attentivement le code
- Testez localement si possible
- Laissez des commentaires constructifs

### Style de review

```
// ❌ Ne pas faire
This code is bad.

// ✅ À faire
Consider using a more descriptive variable name here, as `x` doesn't 
indicate what value it represents. Something like `plantHealthFactor` 
would be clearer.

Voir convention [ici](lien).
```

## Bonus contributions

### Idées issues populaires

- 🎨 Interface web améliorée
- 📊 Meilleure visualisation données
- 🔐 Système d'authentification
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

- ✅ Être respectueux et constructif
- ✅ Accepter les critiques
- ✅ Aider autres contributeurs
- ✅ Attribuer crédits

### Licensing

Tout code contribué doit être compatible avec la licence du projet.

## Questions ?

- 📧 Email : contact@greendesk.dev
- 💬 Discord : [Lien]
- 🐛 Issues : Poser des questions en créant une issue avec label `question`

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
