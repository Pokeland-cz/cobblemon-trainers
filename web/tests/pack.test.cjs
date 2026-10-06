const { test } = require('node:test');
const assert = require('node:assert/strict');
const { editor, Archive, plain } = require('./helpers.cjs');

function setup() {
  const result = editor(['assets', 'pack'], { JSZip: Archive });
  const blobs = new Map();
  result.Assets.put = async (id, blob) => blobs.set(id, blob);
  result.Assets.get = async id => blobs.get(id);
  result.Assets.drop = async id => blobs.delete(id);
  return { ...result, blobs };
}

test('zip exports data, translations and both resource formats without mod metadata', async () => {
  const { Pack } = setup();
  Pack.add('trainer', 'league/erika', { name: 'Erika' });
  Pack.add('category', 'league', { order: 1 });
  Pack.add('intro', 'champion', { duration: 80 });
  Pack.add('advancement', 'badge', { criteria: {} });
  Pack.setTranslation('fr_fr', 'trainer.erika', 'Érika');
  Pack.state.packOrder = '-4';
  const files = (await Pack.build()).files;
  assert.deepEqual(JSON.parse(files['pack.mcmeta']).pack.supported_formats, { min_inclusive: 34, max_inclusive: 48 });
  assert.equal(JSON.parse(files['data/mon_pack/cobblemontrainers/trainers/pack.json']).order, -4);
  for (const path of ['trainers/league/erika', 'trainers/league/category', 'intro/champion']) {
    assert.ok(files[`data/mon_pack/cobblemontrainers/${path}.json`]);
  }
  assert.ok(files['data/mon_pack/advancement/badge.json']);
  assert.equal(JSON.parse(files['assets/mon_pack/lang/fr_fr.json'])['trainer.erika'], 'Érika');
  assert.equal(files['fabric.mod.json'], undefined);
  assert.equal(files['META-INF/neoforge.mods.toml'], undefined);
});

test('jar exports exactly two loader descriptors with optional dependencies', async () => {
  const { Pack } = setup();
  Pack.state.archive = 'jar';
  Pack.state.namespace = '1.my-pack';
  const files = (await Pack.build()).files;
  assert.deepEqual(Object.keys(files).sort(), ['META-INF/neoforge.mods.toml', 'README.txt', 'fabric.mod.json', 'pack.mcmeta']);
  const fabric = JSON.parse(files['fabric.mod.json']);
  assert.equal(fabric.name, Pack.state.description);
  assert.deepEqual(fabric.recommends, { 'cobblemon-trainers': '*' });
  const toml = files['META-INF/neoforge.mods.toml'];
  assert.match(toml, /modId="pack_1_my_pack"/);
  assert.match(toml, /modId="cobblemon_trainers"\s+type="optional"/);
});

test('NeoForge metadata quotes user descriptions as valid single-line strings', async () => {
  const { Pack } = setup();
  Pack.state.archive = 'jar';
  Pack.state.description = 'My "pack"\\folder\nTriple: \'\'\'\t\x7f';
  const toml = (await Pack.build()).files['META-INF/neoforge.mods.toml'];
  for (const field of ['displayName', 'description']) {
    const encoded = toml.split('\n').find(line => line.startsWith(field + '=')).slice(field.length + 1);
    assert.equal(JSON.parse(encoded), Pack.state.description);
    assert.ok(!encoded.includes('\x7f'));
  }
});

test('renaming an asset to its own name is stable and collisions remain unique', async () => {
  const { Pack } = setup();
  const first = await Pack.addAsset({ name: 'Champion.ogg', size: 123 }, 'music');
  const second = await Pack.addAsset({ name: 'Champion.ogg', size: 123 }, 'music');
  assert.equal(second.name, 'champion_2');
  Pack.renameAsset(first.id, 'champion');
  assert.equal(first.name, 'champion');
  Pack.renameAsset(second.id, 'champion');
  assert.equal(second.name, 'champion_2');
});

test('sound export connects stored bytes, stream settings and references', async () => {
  const { Pack, Assets } = setup();
  const music = await Pack.addAsset({ name: 'Élite.ogg', size: 123 }, 'music');
  const sound = await Pack.addAsset({ name: 'Impact.ogg', size: 12 }, 'sound');
  const files = (await Pack.build()).files;
  const sounds = JSON.parse(files['assets/mon_pack/sounds.json']);
  assert.equal(Assets.reference(music, 'mon_pack'), 'mon_pack:battle_music.elite');
  assert.equal(sounds['battle_music.elite'].sounds[0].stream, true);
  assert.equal(sounds['intro.impact'].sounds[0].stream, false);
  assert.ok(files[Assets.path(music, 'mon_pack')]);
  assert.ok(files[Assets.path(sound, 'mon_pack')]);
});

test('JSON import, duplication and removal do not share mutable documents', () => {
  const { Pack } = setup();
  const entry = Pack.readJson('{"team":["Eevee"]}', 'ERIKA.json');
  assert.equal(entry.path, 'erika');
  Pack.duplicate(entry.key);
  Pack.current().doc.team.push('Pikachu');
  assert.deepEqual(plain(entry.doc.team), ['Eevee']);
  Pack.remove(Pack.current().key);
  assert.equal(Pack.current().key, entry.key);
  Pack.remove(entry.key);
  assert.equal(Pack.current(), null);
});

test('exported data and translations import back with their category and pack order', async () => {
  const { Pack } = setup();
  Pack.state.packOrder = '3';
  Pack.add('trainer', 'league/erika', { team: ['Eevee'] });
  Pack.add('category', 'league', { name: 'League', order: 2 });
  Pack.setTranslation('fr_fr', 'trainer.erika', 'Érika');
  const archive = await Pack.build();
  const restored = setup().Pack;
  await restored.readZip(archive);
  assert.equal(restored.state.packOrder, '3');
  assert.equal(restored.state.archive, 'zip');
  assert.deepEqual(plain(restored.state.files.map(({ kind, path, doc }) => ({ kind, path, doc }))),
    plain(Pack.state.files.map(({ kind, path, doc }) => ({ kind, path, doc }))));
  assert.deepEqual(plain(restored.state.lang), plain(Pack.state.lang));
});

test('used translation keys keep first occurrence order without duplicates', () => {
  const { Pack } = setup();
  Pack.add('trainer', 'erika', { name: 'trainer.erika', messages: { greeting: 'trainer.greeting', defeat: 'trainer.erika' } });
  Pack.add('category', 'league', { name: 'trainer.erika' });
  assert.deepEqual(plain(Pack.usedKeys()), ['trainer.erika', 'trainer.greeting']);
});

test('clearing a pack removes its asset bytes as well as metadata', async () => {
  const { Pack, blobs } = setup();
  await Pack.addAsset({ name: 'skin.png', size: 42 }, 'skin');
  Pack.add('trainer', 'erika', {});
  await Pack.clear();
  assert.equal(blobs.size, 0);
  assert.equal(Pack.state.assets.length, 0);
  assert.equal(Pack.state.files.length, 0);
  assert.equal(Pack.current(), null);
});
