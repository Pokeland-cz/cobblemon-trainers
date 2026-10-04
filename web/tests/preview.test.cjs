const { test } = require('node:test');
const assert = require('node:assert/strict');
const { editor, plain } = require('./helpers.cjs');

function preview() {
  let created = 0;
  const context = {
    save() {}, restore() {}, clearRect() {}, drawImage() {}, fillRect() {}, translate() {}, rotate() {},
    measureText(value) { return { width: value.length * 6 }; }
  };
  const canvas = () => ({ width: 640, height: 360, getContext: () => context });
  class Image {
    constructor() { this.width = 64; this.height = 32; }
    set src(value) { queueMicrotask(() => this.onload()); }
  }
  const { Preview } = editor(['preview'], {
    Image, document: { createElement() { created++; return canvas(); } }
  });
  return { Preview, canvas: canvas(), created: () => created };
}

test('missing offset coordinates default to zero like the game renderer', () => {
  const { Preview } = preview();
  assert.deepEqual(plain(Preview.rest({})), [320, 180]);
  assert.deepEqual(plain(Preview.rest({ offset: [] })), [320, 180]);
  assert.deepEqual(plain(Preview.rest({ anchor: 'top-right', offset: [-10] })), [630, 0]);
  assert.deepEqual(plain(Preview.rest({ anchor: 'bottom-left', offset: [10, -20] })), [10, 340]);
});

test('stable depth order keeps file order for ties without mutating layers', () => {
  const { Preview } = preview();
  const layers = [{ z: 2 }, {}, { z: 2 }, { z: -1 }];
  assert.deepEqual(plain(Preview.orderedLayers(layers).map(entry => entry.index)), [3, 1, 0, 2]);
  assert.deepEqual(layers, [{ z: 2 }, {}, { z: 2 }, { z: -1 }]);
});

test('rotated image bounds and slanted fills include their visible corners', () => {
  const { Preview, canvas } = preview();
  const ctx = canvas.getContext('2d');
  const [w, h] = Preview.extent(ctx, { type: 'image', width: 20, height: 40, rotation: 90 });
  assert.ok(Math.abs(w - 40) < 1e-9);
  assert.ok(Math.abs(h - 20) < 1e-9);
  assert.deepEqual(plain(Preview.extent(ctx, { type: 'fill', width: 100, height: 20, slant: -2 })), [140, 20]);
});

test('repeated frames reuse tinted images and replacing a texture invalidates the tint', async () => {
  const { Preview, canvas, created } = preview();
  Preview.give('pack:image', 'first');
  Preview.texture('pack:image');
  await Promise.resolve();
  const layer = { type: 'image', texture: 'pack:image', color: '#ff0000', offset: [0, 0] };
  const scene = { layers: [layer] };
  const first = Preview.frame(canvas, scene, 20, {});
  assert.equal(first[0].x, 320);
  assert.equal(first[0].y, 180);
  assert.equal(created(), 2); // One scene canvas and one tint.
  for (let i = 0; i < 60; i++) Preview.frame(canvas, scene, 20, {});
  layer.color = '#FF0000';
  Preview.frame(canvas, scene, 20, {});
  assert.equal(created(), 2);
  Preview.give('pack:image', 'replacement');
  Preview.texture('pack:image');
  await Promise.resolve();
  Preview.frame(canvas, scene, 20, {});
  assert.equal(created(), 3);
});

test('editing many colours evicts old tints instead of retaining every canvas', async () => {
  const { Preview, canvas, created } = preview();
  Preview.give('pack:image', 'first');
  Preview.texture('pack:image');
  await Promise.resolve();
  const layer = { type: 'image', texture: 'pack:image' };
  for (let colour = 0; colour < 9; colour++) {
    layer.color = '#' + colour.toString(16).padStart(6, '0');
    Preview.frame(canvas, { layers: [layer] }, 20, {});
  }
  const before = created();
  layer.color = '#000000';
  Preview.frame(canvas, { layers: [layer] }, 20, {});
  assert.equal(created(), before + 1);
});
