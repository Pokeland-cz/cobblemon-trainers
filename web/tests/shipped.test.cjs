const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const vm = require('node:vm');
const { readCatalog, renderCatalog } = require('../sync-shipped.cjs');

function fixture(t) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'ct-shipped-'));
  t.after(() => {
    assert.equal(path.dirname(path.resolve(root)), path.resolve(os.tmpdir()));
    assert.ok(path.basename(root).startsWith('ct-shipped-'));
    fs.rmSync(root, { recursive: true, force: true });
  });
  const assets = path.join(root, 'common/src/main/resources/assets/cobblemon-trainers');
  const textures = path.join(assets, 'textures/gui/intro');
  fs.mkdirSync(textures, { recursive: true });
  fs.writeFileSync(path.join(assets, 'sounds.json'), '{"battle_music.example":{"sounds":[]}}');
  return { root, assets, textures };
}

test('texture suggestions follow additions, removals and nested paths', t => {
  const { root, textures } = fixture(t);
  fs.writeFileSync(path.join(textures, 'old.png'), '');
  assert.deepEqual(readCatalog(root).textures, ['cobblemon-trainers:textures/gui/intro/old.png']);
  fs.unlinkSync(path.join(textures, 'old.png'));
  fs.mkdirSync(path.join(textures, 'nested'));
  fs.writeFileSync(path.join(textures, 'nested/new.png'), '');
  fs.writeFileSync(path.join(textures, 'circle.png'), '');
  fs.writeFileSync(path.join(textures, 'notes.txt'), '');
  assert.deepEqual(readCatalog(root).textures, [
    'cobblemon-trainers:textures/gui/intro/circle.png',
    'cobblemon-trainers:textures/gui/intro/nested/new.png'
  ]);
});

test('sound keys are independent of JSON indentation and output is usable without fetch', t => {
  const { root, assets } = fixture(t);
  fs.writeFileSync(path.join(assets, 'sounds.json'), '{"z.sound":{},"battle_music.a":{}}');
  const catalog = readCatalog(root);
  assert.deepEqual(catalog.sounds, ['battle_music.a', 'z.sound']);
  const decoded = vm.runInNewContext(renderCatalog(catalog) + '\nJSON.stringify(SHIPPED)');
  assert.deepEqual(JSON.parse(decoded), catalog);
});

test('missing or malformed resources fail instead of silently writing a stale catalogue', t => {
  const { root, assets } = fixture(t);
  fs.writeFileSync(path.join(assets, 'sounds.json'), '{}');
  assert.throws(() => readCatalog(root), /no sound keys/);
  fs.writeFileSync(path.join(assets, 'sounds.json'), '{');
  assert.throws(() => readCatalog(root), SyntaxError);
});

test('committed catalogue matches the resources shipped with this checkout', () => {
  const actual = fs.readFileSync(path.join(__dirname, '../js/shipped.js'), 'utf8');
  assert.equal(actual.replace(/\r\n/g, '\n'), renderCatalog(readCatalog()),
    'Run node web/sync-shipped.cjs after changing mod textures or sounds');
});
