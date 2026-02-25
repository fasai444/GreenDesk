# Contribuer

## Workflow recommandé

1. Créer une branche feature
2. Développer avec commits atomiques
3. Exécuter `./gradlew clean check`
4. Ouvrir une Pull Request

## Standards de qualité

- Code lisible et minimal
- Pas de régression API non documentée
- Tests adaptés aux changements
- Couverture JaCoCo respectée

## Checklist PR

- [ ] Build local OK
- [ ] Tests OK
- [ ] Couverture JaCoCo OK
- [ ] Documentation mise à jour (`docs/`)
- [ ] Endpoint/contrat API mis à jour si impacté

## Commandes utiles

```bash
./gradlew clean check
./gradlew test jacocoTestReport
```

## Documentation à maintenir

- `docs/api/*` pour la référence endpoints
- `docs/usage/*` pour les scénarios fonctionnels
- `docs/testing.md` pour les métriques et process qualité
