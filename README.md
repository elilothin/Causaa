# CAUSA SUI

**« Chaque effort est une cause. » · Forge-toi.**

Application de sport Android **100 % hors-ligne** (Kotlin + Jetpack Compose + Room), sans compte, sans serveur, sans abonnement, sans caméra ni photo.

## Fonctionnalités implémentées (Cahier des charges v7)

- **Thèmes Bento** : Thème **Beige** (par défaut) et Thème **Sombre**, cartes arrondies à plat sans ombres dures ni bordures épaisses, typographies locales **Archivo** et **JetBrains Mono**.
- **Navigation 5 onglets** : Barre flottante à encoche courbe avec pastille colorée glissante (`Accueil`, `Programme`, `Nutrition`, `Progrès`, `Moi`).
- **Premier lancement & garde-fous** : Pseudo (3–20), sexe, âge (16–90), taille (130–220 cm), poids (35–250 kg), morphotype (+ mini-quiz 3 questions), objectif, jours/semaine (2–6). Aucun déficit calorique sous 18 ans, refus des cibles à IMC < 18,5 ou perte > 1 %/semaine.
- **Programme & Séances Salle / Maison** : Répartitions 2 à 6 jours, 3 niveaux de difficulté (`×0,7`, `×1`, `×1,3`), équivalence maison sans multiplicateurs artificiels et progression d'exercices, **aucun minuteur** (`PASSER` / `SUIVANT` + bilan final avec calories estimées modifiables et XP).
- **Personnage 3D adaptatif sans visage** : Modèle 3D articulé temps réel en Compose Canvas qui s'adapte au sexe, à l'âge, à la taille, au poids/IMC, au morphotype et au stade musculaire (1 à 5 + aura or dès le rang S), avec 3 angles (`Face`, `Profil`, `Dos`), rotation tactile et animations biomécaniques en boucle.
- **Nutrition & Hydratation** : Formules Mifflin-St Jeor + TDEE + macros avec bouton « Pourquoi ? », catalogue local de **200+ aliments** (dont plats courants au Togo et en Afrique de l'Ouest) + saisie directe de calories, jauge d'eau à 8 segments (`+0,25 L`, `+0,5 L`, `+1 L`, `Ma bouteille`, `Annuler`).
- **Ranked à 5 piliers & Avatar** : Questionnaire à curseur gradué (`0 = non compté`), radar 5 axes, 10 rangs (`F` à `Z+`), badge de rang et constructeur d'avatar vectoriel (style bitmoji, sans photo).
- **Progrès** : Courbes `Semaine / Mois` (Poids, Eau, Séances, Rang en marches), calendrier en points, séries, mensurations + courbes et notes libres par date.
- **Ambiance & Sauvegarde** : **200+ punchlines originales**, badges de réussite, sons de validation synthétisés hors-ligne, vibrations configurables, rappels locaux Android, et export / import complet en fichier JSON.

## Installation depuis GitHub Releases

1. Téléchargez le fichier `.apk` depuis l'onglet **Releases** du dépôt GitHub.
2. Sur votre téléphone Android, autorisez l'installation depuis cette source si demandé.
3. Pour mettre à jour plus tard sans perdre vos données, installez la nouvelle version signée avec le même keystore et exportez régulièrement votre sauvegarde JSON depuis le bouton en haut de l'écran.

## Compiler l'APK localement

```bash
./gradlew assembleDebug
```
