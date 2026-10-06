#!/usr/bin/env bash
# Fills web/assets/ from the mod itself.
#
# The editor needs four things the repository already holds: the intro textures a pack may
# name, the eight intros shipped with the mod, the example trainers, and the mod's own
# sounds.json - the list of tracks a trainer may ask for. None of them is copied into the
# repository - they are taken from their one source at publish time, so a track added to the
# mod is offered by the editor without anyone editing a list, and nothing can drift.
#
# Run it before opening web/index.html locally; the Pages workflow runs the same script.
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
root="$(cd "$here/.." && pwd)"
assets="$here/assets"

textures="$root/common/src/main/resources/assets/cobblemon-trainers/textures/gui/intro"
intros="$root/common/src/main/resources/data/cobblemon-trainers/cobblemontrainers/intro"
examples="$root/examples/cobblemonrlm/data/cobblemonrlm/cobblemontrainers/trainers"

rm -rf "$assets/intro" "$assets/intros" "$assets/examples"
mkdir -p "$assets/intro" "$assets/intros" "$assets/examples"

# The mod's own identity, taken where it already lives rather than copied into the site.
cp "$root/assets/logo.png" "$assets/logo.png"
cp "$root/common/src/main/resources/assets/cobblemon-trainers/icon.png" "$assets/icon.png"

cp -R "$textures/." "$assets/intro/"
cp "$intros"/*.json "$assets/intros/"
find "$examples" -name '*.json' ! -name 'category.json' -exec cp {} "$assets/examples/" \;

# Keep menu suggestions in step with the files just copied, including deleted textures.
# A script also works from file://, unlike a fetched manifest.
node "$here/sync-shipped.cjs"

# The manifest is what the templates dropdown reads: a listing, since a static host has none.
{
  echo '{'
  echo '  "intros": ['
  first=1
  for file in "$assets/intros"/*.json; do
    name="$(basename "$file" .json)"
    [ $first -eq 1 ] || echo ','
    first=0
    printf '    { "id": "%s", "file": "assets/intros/%s.json" }' "$name" "$name"
  done
  echo ''
  echo '  ],'
  echo '  "trainers": ['
  first=1
  for file in "$assets/examples"/*.json; do
    name="$(basename "$file" .json)"
    [ $first -eq 1 ] || echo ','
    first=0
    printf '    { "id": "%s", "file": "assets/examples/%s.json" }' "$name" "$name"
  done
  echo ''
  echo '  ]'
  echo '}'
} > "$assets/manifest.json"

echo "web/assets: $(find "$assets/intro" -name '*.png' | wc -l) textures, $(ls "$assets/intros" | wc -l) intros, $(ls "$assets/examples" | wc -l) trainers"
