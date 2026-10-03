# Exemple : un dresseur avec un modèle personnalisé

Ce pack ajoute **Explorateur** (`custom_model:explorer`), avec un Évoli de niveau 15.
Son chapeau et son sac à dos font partie de sa géométrie Bedrock ; son poser et ses
animations sont propres au pack. Sa texture reprend le skin par défaut fourni par le mod.
Le combat utilise l'intro `bw`, ce qui permet de vérifier le modèle dans le monde, dans
la fiche du Battle Phone et dans l'écran de versus.

## Installation

Utilise une version de Cobblemon Trainers qui prend en charge `skin.type: model`, sur
le serveur et sur chaque client, avec Cobblemon 1.8.1 et Minecraft 1.21.1.

- **Solo :** pose `exemple_custom_model_datapack.zip` dans `mods/`, puis relance le jeu.
- **Serveur :** pose le ZIP dans `mods/` du serveur et de chaque client, puis redémarre.
  Autre possibilité : le ZIP dans `datapacks/` du monde côté serveur, et dans
  `resourcepacks/` côté client, où il faut l'activer.

Le modèle est un asset client : installer le datapack uniquement sur le serveur ne suffit pas.
N'installe qu'une copie par côté. Ce ZIP ne contient pas de `fabric.mod.json` et fonctionne
avec le chargement de packs du mod sur Fabric et NeoForge.

Avec les permissions opérateur :

```mcfunction
/cobblemontrainers spawn custom_model:explorer
```

Le dresseur est aussi visible et appelable dans le Battle Phone. Les combats sont rejouables.

## Modifier le modèle

| Fichier | Rôle |
| --- | --- |
| `data/custom_model/cobblemontrainers/trainers/explorer.json` | Dresseur, skin, équipe et combat |
| `assets/custom_model/bedrock/npcs/variations/explorer/0_explorer.json` | Lie l'identifiant du skin au modèle, au poser et à la texture |
| `assets/custom_model/bedrock/npcs/models/explorer.geo.json` | Modèle à importer dans Blockbench au format Bedrock |
| `assets/custom_model/textures/npcs/explorer.png` | Texture 64 × 64 |
| `assets/custom_model/bedrock/npcs/posers/explorer.json` | Poses et animations déclenchées par Cobblemon |
| `assets/custom_model/bedrock/npcs/animations/custom_model_explorer.animation.json` | Repos, salut et défaite |
| `assets/custom_model/lang/` | Nom et dialogues français / anglais |

Garde les noms d'os référencés par le poser et les animations. `skin.value` pointe vers
le `name` du resolver : `custom_model:explorer`. Pour remplacer la géométrie, change le fichier
`.geo.json`, pas la classe NPC. La hitbox reste celle d'un joueur.

Recharge les ressources client avec `F3 + T` après modification des assets. Pour une modification
du dresseur, utilise `/reload`, puis fais-le réapparaître. La fiche du Battle Phone montre le
modèle 3D ; sa petite vignette affiche `?`, car ce modèle n'a pas de visage de skin joueur.

## Depuis le dépôt

```powershell
.\gradlew.bat customModelExampleDatapack
```

Produit `build/dist/exemple_custom_model_datapack.zip`. `runClient` et `runServer` copient
automatiquement ce pack dans `fabric/run/mods/custom_model/`. Après une modification du pack
source, relance `:fabric:copyCustomModelExamplePack` avant de recharger le jeu.

Voir [le guide des skins](../../docs/DATAPACK.md#skins) et [la version anglaise](README.en.md).
