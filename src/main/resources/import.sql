-- Insertion des espèces de base
INSERT INTO species (id, name, optimal_water_needs, optimal_temperature) VALUES ('sp-1', 'Chêne', 50.0, 22.0);

-- Insertion du catalogue d'effets prédéfinis
INSERT INTO effects (id, name, description, duration_hours, temperature_modifier, water_modifier, growth_rate_modifier, is_custom) 
VALUES ('eff-shade', 'Shade 6h', 'Réduit l''exposition au soleil', 6, -5.0, 0.0, 0.0, false);

INSERT INTO effects (id, name, description, duration_hours, temperature_modifier, water_modifier, growth_rate_modifier, is_custom) 
VALUES ('eff-fert', 'Fertilizer', 'Booste la croissance', 24, 0.0, 0.0, 15.0, false);