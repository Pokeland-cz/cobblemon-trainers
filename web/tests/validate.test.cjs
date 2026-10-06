const { test } = require('node:test');
const assert = require('node:assert/strict');
const { editor } = require('./helpers.cjs');

test('trainer validation catches reserved paths, bad gimmicks and unqualified rewards', () => {
  const { Validate } = editor(['validate']);
  const found = Validate.file({ kind: 'trainer', path: 'league/category', doc: {
    battle: { gimmicks: ['dynamax'] }, rewards: [{ item: 'diamond' }]
  } });
  assert.equal(found.filter(message => message.level === 'error').length, 3);
});

test('valid partial trainers and intros require no registry access', () => {
  const { Validate } = editor(['validate']);
  for (const kind of ['trainer', 'intro']) {
    assert.equal(Validate.file({ kind, path: 'sample', doc: {} }).filter(message => message.level === 'error').length, 0);
  }
});

test('reward validation accepts item components while checking the namespaced item ID', () => {
  const { Validate } = editor(['validate']);
  const found = Validate.file({ kind: 'trainer', path: 'erika', doc: {
    rewards: [{ item: 'minecraft:diamond_sword[minecraft:damage=7]' }]
  } });
  assert.equal(found.filter(message => message.level === 'error').length, 0);
});

test('pack validation detects overwrite and missing translations in one key scan', () => {
  const { Validate } = editor(['validate']);
  let scans = 0;
  const found = Validate.pack({ namespace: 'pack', packOrder: '', files: [
    { kind: 'trainer', path: 'erika' }, { kind: 'trainer', path: 'erika' }
  ], lang: { fr_fr: { 'trainer.erika': 'Érika' }, en_us: {} }, usedKeys() {
    scans++;
    return ['trainer.erika', 'trainer.missing'];
  } });
  assert.equal(scans, 1);
  assert.ok(found.some(message => message.level === 'error' && message.en.includes('overwrites')));
  assert.ok(found.some(message => message.level === 'warn' && message.en.includes('no text')));
});
