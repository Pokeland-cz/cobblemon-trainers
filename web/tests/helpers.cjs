const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

/** Load the same classic scripts as index.html, with a fresh browser state per test. */
function editor(scripts, globals = {}) {
  const saved = new Map();
  const context = vm.createContext({
    localStorage: {
      getItem: key => saved.get(key) ?? null,
      setItem: (key, value) => saved.set(key, value)
    },
    ...globals
  });
  for (const script of scripts) {
    vm.runInContext(fs.readFileSync(path.join(__dirname, '../js', script + '.js'), 'utf8'), context, { filename: script + '.js' });
  }
  return vm.runInContext('({' + scripts.map(name => name[0].toUpperCase() + name.slice(1)).join(',') + '})', context);
}

// Capture the archive API boundary. Compression and IndexedDB belong to their libraries.
class Archive {
  constructor() { this.files = {}; }
  file(name, value) { this.files[name] = value; return this; }
  static async loadAsync(archive) {
    return { files: Object.fromEntries(Object.entries(archive.files).map(([name, value]) => [name, {
      name, dir: false, async: async () => value
    }])) };
  }
}

const plain = value => JSON.parse(JSON.stringify(value));
module.exports = { editor, Archive, plain };
