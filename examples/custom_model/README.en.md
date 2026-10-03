# Example: a trainer with a custom model

This pack adds **Explorer** (`custom_model:explorer`), with a level 15 Eevee.
The hat and backpack are part of its Bedrock geometry; its poser and animations belong
to this pack. Its texture reuses the default skin supplied by the mod.
The battle uses the `bw` intro, so the model can be checked in the world, in the Battle
Phone detail view and on the versus screen.

## Installation

Use a Cobblemon Trainers version supporting `skin.type: model` on the server and every
client, with Cobblemon 1.8.1 and Minecraft 1.21.1.

- **Single player:** put `exemple_custom_model_datapack.zip` in `mods/`, then restart.
- **Server:** put the ZIP in the server's and every client's `mods/`, then restart.
  Alternatively, put it in the world's `datapacks/` on the server and in `resourcepacks/`
  on each client, where it must be enabled.

The model is a client asset: installing the datapack only on the server is not enough.
Install only one copy per side. This ZIP has no `fabric.mod.json` and works with the mod's
pack loader on Fabric and NeoForge.

With operator permissions:

```mcfunction
/cobblemontrainers spawn custom_model:explorer
```

The trainer can also be found and called in the Battle Phone. Battles can be repeated.

## Editing the model

| File | Role |
| --- | --- |
| `data/custom_model/cobblemontrainers/trainers/explorer.json` | Trainer, skin, team and battle |
| `assets/custom_model/bedrock/npcs/variations/explorer/0_explorer.json` | Maps the skin ID to geometry, poser and texture |
| `assets/custom_model/bedrock/npcs/models/explorer.geo.json` | Model to import in Blockbench as Bedrock geometry |
| `assets/custom_model/textures/npcs/explorer.png` | 64 × 64 texture |
| `assets/custom_model/bedrock/npcs/posers/explorer.json` | Poses and animations triggered by Cobblemon |
| `assets/custom_model/bedrock/npcs/animations/custom_model_explorer.animation.json` | Idle, wave and defeat |
| `assets/custom_model/lang/` | French / English name and dialogue |

Keep the bone names used by the poser and animations. `skin.value` refers to the resolver's
`name`: `custom_model:explorer`. To replace the geometry, change the `.geo.json` file, not the
NPC class. The hitbox remains player-sized.

Reload client resources with `F3 + T` after editing assets. For trainer changes, use `/reload`
and spawn it again. The Battle Phone detail view shows the 3D model; its small thumbnail
shows `?` because this model has no player-skin face.

## From the repository

```powershell
.\gradlew.bat customModelExampleDatapack
```

Produces `build/dist/exemple_custom_model_datapack.zip`. `runClient` and `runServer` copy this
pack into `fabric/run/mods/custom_model/` automatically. After editing the source pack, run
`:fabric:copyCustomModelExamplePack` again before reloading the game.

See [the skin guide](../../docs/en/DATAPACK.md#skins) and [the French version](README.md).
