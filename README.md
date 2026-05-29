# Meds And Herbs: Afflictions

**Forge 1.20.1** — modid: `mhafflictions` — v1.1.0

Addon de gestion de la santé pour serveur RP. Ajoute 24 effets médicaux custom, un système de déclencheurs de blessures basé sur les actions du joueur, et une intégration optionnelle avec [Meds And Herbs](https://modrinth.com/mod/meds-and-herbs).

---

## Fonctionnement

Le mod est **standalone** — il fonctionne sans Meds And Herbs. Si M&H est installé, ses items soignent automatiquement les effets de ce mod.

### Déclencheurs de blessures

| Événement | Effet | Probabilité |
|---|---|---|
| Chute > 10 blocs | `broken_bone` + `bleeding` | 100 % |
| Chute 6–10 blocs | `broken_bone` | 60 % |
| Projectile reçu | `bleeding` | 50 % |
| Coup ≥ 6 dégâts | `laceration` | 35 % |
| Coup ≥ 3 dégâts | `bleeding` | 25 % |
| Feu / Lave | `burns` | 100 % |
| Viande crue mangée | `parasites` | 15 % |
| Eau (voir ci-dessous) | `bacterial_infection` | Progressif |

### Système eau — Hypothermie

Conditions : être dans l'eau avec **≥ 18/27 blocs d'eau dans le cube 3×3×3 centré sur le joueur** (petite flaque = aucun effet). Le compteur se réinitialise dès que le joueur quitte la zone.

| Durée continue | Effet | Chance |
|---|---|---|
| 60 s | Bacterial Infection | 5 % |
| 90 s | Bacterial Infection | 10 % |
| 120 s | Bacterial Infection | 30 % |
| 150 s | Bacterial Infection | 60 % |
| 180 s | Bacterial Infection | **100 % garanti** |
| 210 s | Bacterial Infection Lv.2 + Nausée + 6 HP | **100 %** |
| 240 s | **MORT — Hypothermie** | 100 % |

---

## Effets custom (24)

### Effets négatifs — Dégâts

| ID | Nom | Dégâts |
|---|---|---|
| `bleeding` | Saignement | 1 HP/s (1.5 avec lacération) |
| `internal_bleeding` | Hémorragie Interne | 2 HP/s |
| `thrombosis` | Thrombose | 1.5 HP/s |
| `hpp` | Poison Haute Puissance | 2 HP/s — mortel ~10s |
| `burns` | Brûlures | 1 HP/2s |
| `bacterial_infection` | Infection Bactérienne | 0.5 HP/2s |
| `laceration` | Lacération | Aggrave bleeding ×1.5 |
| `parasites` | Parasites | 0.5 HP/3s + faim |
| `methanol_poisoning` | Empoisonnement Méthanol | 1 HP/s |
| `mushroom_poisoning` | Empoisonnement Champignons | 0.5 HP/2s + nausée |

### Effets négatifs — Debuffs passifs

| ID | Nom | Effet passif |
|---|---|---|
| `broken_bone` | Os Cassé | Slowness 2 si en mouvement, 1 HP/s si bouge |
| `blood_loss` | Perte de Sang | Weakness + Slowness + Nausée |
| `opium_addiction` | Addiction à l'Opium | Déclenche sevrage à l'expiration |
| `opium_withdrawal` | Sevrage à l'Opium | Weakness II + Slowness II |
| `effect_belladonna_berry` | Belladone | Confusion + Cécité |
| `ultraviolet_vulnerability` | Vulnérabilité UV | Passif |

### Effets positifs

| ID | Nom | Effet |
|---|---|---|
| `hpa` | Antidote Haute Puissance | Contre HPP |
| `painkiller` | Antidouleur | Masque la douleur |
| `adrenaline` | Adrénaline | Speed II + Resistance I |
| `antibiotics` | Antibiotiques | Statut post-infection |
| `antiseptic` | Antiseptique | Prévient l'infection |
| `immune` | Immunisé | Immunité temporaire |
| `bone_heal` | Guérison Osseuse | Retire `broken_bone` à l'expiration |
| `beverage_drink` | Boisson | Statut consommation |

---

## Intégration Meds And Herbs (optionnelle)

Si M&H est installé, ses items soignent les effets de ce mod. Les items sont **consommés à l'usage** (mode créatif exempt).

| Item M&H | Action |
|---|---|
| Dressing (toutes variantes) | Retire `bleeding` (Cheap : 50 %) |
| Splint | Donne `bone_heal` 3 min → retire `broken_bone` |
| Medkit Novice | +5 HP, retire `bleeding` + `laceration` |
| Medkit Advanced | +10 HP, retire `bleeding` + `internal_bleeding` + `thrombosis` + `laceration` |
| Medkit Expert | +15 HP, retire tous les effets majeurs |
| Syringe Vinca | Retire `internal_bleeding` — ou donne `thrombosis` — ou **tue** si `thrombosis` actif |
| Syringe Sweet Clover | Retire `thrombosis` — ou donne `internal_bleeding` — ou **tue** si `internal_bleeding` actif |
| Syringe Penicillin | Retire `bacterial_infection` → donne `antibiotics` |
| Syringe Blood | +4 HP, retire `blood_loss` |
| Syringe HPA | Retire `hpp` → donne `hpa` |
| Syringe Artemisia | Retire `parasites` |
| Syringe Ethanol | Retire `methanol_poisoning` |
| Syringe Adrenaline | Donne `adrenaline` (2e dose = 10 HP d'arythmie) |
| Syringe Methanol | Donne `methanol_poisoning` (2e dose = mort instantanée) |
| Syringe Chamomile | Retire Nausée / Faim / Faiblesse / Lenteur |

### ⚠️ Contre-indications mortelles

| Action | Condition | Résultat |
|---|---|---|
| Syringe Vinca | `thrombosis` active | **MORT INSTANTANÉE** |
| Syringe Sweet Clover | `internal_bleeding` actif | **MORT INSTANTANÉE** |
| Syringe Adrenaline | `adrenaline` déjà active | 10 HP d'arythmie |
| Syringe Methanol | `methanol_poisoning` actif | **MORT INSTANTANÉE** |
| Eau 240 s (zone 3×3×3) | Continu | **MORT — Hypothermie** |

---

## Tester avec commandes

```
/effect give @s mhafflictions:bleeding 9999 0
/effect give @s mhafflictions:broken_bone 9999 0
/effect give @s mhafflictions:hpp 30 0
```

---

## Dépendances

| Mod | Obligatoire |
|---|---|
| Forge 1.20.1-47.x | ✅ |
| Meds And Herbs 2.0.x | ❌ Optionnel |

---

*Auteur : Akirabane*
