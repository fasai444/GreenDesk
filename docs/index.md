# GreenDesk Documentation

<section class="landing-hero">
	<p class="landing-kicker">Documentation technique & opérationnelle</p>
	<h2>Plateforme GreenDesk</h2>
	<p>
		GreenDesk est une plateforme Spring Boot + MongoDB pour la gestion des espèces,
		plantes, forêts, effets/stimuli et simulations d'écosystème, avec endpoints API,
		indicateurs qualité et guides d'exploitation.
	</p>
	<div class="landing-cta-row">
		<a href="#/getting-started">Démarrer rapidement</a>
		<a href="#/api/overview">Explorer l'API</a>
		<a href="#/architecture">Voir l'architecture</a>
	</div>
</section>

<section class="landing-kpis">
	<div><strong>Java 21</strong><span>Runtime principal</span></div>
	<div><strong>Spring Boot 3.3.3</strong><span>Back-end applicatif</span></div>
	<div><strong>MongoDB</strong><span>Stockage métier</span></div>
	<div><strong>Gradle + JaCoCo</strong><span>Build & qualité</span></div>
</section>

## Parcours recommandé

<section class="landing-grid">
	<a class="landing-card" href="#/getting-started">
		<h3>Démarrage</h3>
		<p>Installer le projet, lancer l'application et exécuter un premier flux de validation.</p>
	</a>
	<a class="landing-card" href="#/installation">
		<h3>Installation</h3>
		<p>Pré-requis, configuration locale, variables, profils et stratégies d'exécution.</p>
	</a>
	<a class="landing-card" href="#/architecture">
		<h3>Architecture</h3>
		<p>Vue d'ensemble technique, composants métier et diagrammes Mermaid interactifs.</p>
	</a>
	<a class="landing-card" href="#/api/overview">
		<h3>Référence API</h3>
		<p>Organisation des endpoints, conventions de payloads et exemples de requêtes.</p>
	</a>
	<a class="landing-card" href="#/usage/concepts">
		<h3>Guide d'usage</h3>
		<p>Concepts métier, espèces, plantes, forêts et effets agronomiques.</p>
	</a>
	<a class="landing-card" href="#/testing">
		<h3>Tests & qualité</h3>
		<p>Stratégie de tests, couverture, bonnes pratiques et suivi du statut qualité.</p>
	</a>
	<a class="landing-card" href="#/reports/greenhouse-ops-report">
		<h3>Rapports</h3>
		<p>Rapports techniques consolidés, validation opérationnelle et améliorations livrées.</p>
	</a>
	<a class="landing-card" href="#/docker">
		<h3>Docker</h3>
		<p>Exécution conteneurisée, services dépendants et flux d'intégration locale.</p>
	</a>
</section>

## Aperçu visuel

<section class="screenshot-grid">
	<a class="screenshot-card" href="#/screenshots">
		<img src="assets/images/site-home.png" alt="Page parcours de l'application GreenDesk" />
		<span>Parcours utilisateur</span>
	</a>
	<a class="screenshot-card" href="#/screenshots">
		<img src="assets/images/site-simulation.png" alt="Page simulation GreenDesk" />
		<span>Simulation</span>
	</a>
	<a class="screenshot-card" href="#/screenshots">
		<img src="assets/images/site-dashboard.png" alt="Tableau de bord GreenDesk" />
		<span>Tableau de bord</span>
	</a>
	<a class="screenshot-card" href="#/screenshots?id=step-7">
		<img src="assets/images/site-swagger-ui.png" alt="Swagger API GreenDesk" />
		<span>Swagger API</span>
	</a>
	<a class="screenshot-card" href="#/screenshots?id=step-8">
		<img src="assets/images/site-jacoco-coverage.png" alt="Couverture de code JaCoCo" />
		<span>Couverture JaCoCo</span>
	</a>
</section>

👉 Galerie complète: [Captures d'écran](screenshots.md)

## Liens utiles

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Application: `http://localhost:8080/`
- Mongo Express (Docker): `http://localhost:8081`

## Périmètre fonctionnel

- Gestion CRUD des espèces et plantes.
- Gestion des forêts (grille, positionnement et cycle saisonnier).
- Effets agronomiques (catalogue + personnalisés) et stimulus.
- Alertes plantes, mesures capteurs et simulation d'écosystème.
- KPIs Greenhouse via `/api/greenhouse/*`.

## Convention de lecture

- Les exemples d'API utilisent `curl`.
- Les payloads sont minimaux pour être exécutables directement.
- Les rapports d'évolution sont centralisés dans `docs/reports/`.
- Les diagrammes Mermaid sont présents dans `architecture.md`, `testing.md` et `reports/greenhouse-ops-report.md`.
