# Meds And Herbs: Afflictions

Addon de blessures et d'afflictions pour serveurs RP Minecraft, conçu pour s'intégrer au mod **Meds and Herbs**.

Ce mod ajoute un système de blessures réalistes (saignements, fractures, infections, intoxications…), des déclencheurs automatiques, et une mécanique de **glace fragile** au-dessus de l'eau.

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-62B47A)
![Forge](https://img.shields.io/badge/Forge-47.4.x-1E2D4F)
![Version](https://img.shields.io/badge/version-1.2.0-blue)

## Mod ID
`mhafflictions`

## Compatibilité
- Minecraft 1.20.1
- Forge 47.4.x
- Meds and Herbs (optionnel — détecté automatiquement s'il est présent)

## Fonctionnalités

### 24 effets personnalisés
16 néfastes (bleeding, internal bleeding, broken bone, blood loss, thrombosis, bacterial infection, laceration, burns, HPP, parasites, methanol/mushroom poisoning, opium addiction/withdrawal, belladonna, UV vulnerability) et 8 bénéfiques (HPA, painkiller, adrenaline, antibiotics, antiseptic, immune, bone heal, beverage).

### Déclencheurs automatiques
- **Chute** : > 10 blocs → fracture + saignement ; > 6 blocs → 60 % de fracture
- **Feu** → brûlures · **Projectile** → 50 % saignement
- **Mêlée** : ≥ 6 dégâts → 35 % lacération ; ≥ 3 dégâts → 25 % saignement
- **Eau stagnante** → infection progressive
- **Viande crue** → 15 % de parasites

### Intégration Meds and Herbs
~30 seringues, pansements, medkits et attelle reconnus par namespace et reliés aux effets du mod.

### Glace fragile
Rester immobile sur de la glace posée sur l'eau la fissure selon la profondeur d'eau (1 bloc → 60 s … 5+ → instantané). La glace se brise puis réapparaît 1 s plus tard — pouvant piéger le joueur sous l'eau.

## Changelog

### 1.2.0
- **Équilibrage majeur des dégâts** : afflictions nettement adoucies (bleeding 1 HP / 15 s, internal 1 HP / 7 s, etc.)
- **Plancher de survie** : aucune affliction ne peut tuer — elles laissent le joueur à 1 HP minimum. Seules les vraies sources externes (chute, combat, noyade) achèvent.
- **Suppression de la mort instantanée par eau stagnante** : l'hypothermie passe désormais uniquement par l'infection.
- Broken Bone, Burns, Parasites, Mushroom : intervalles de dégâts allongés.

### 1.1.0
- Ajout de la mécanique de **glace fragile** au-dessus de l'eau.

## Licence
Tous droits réservés — Akirabane.
