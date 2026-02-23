// mermaid-bundle.js
// Attempts to load a local copy of mermaid (assets/js/mermaid.min.js), then falls back
// to an absolute /assets path, then to the CDN. Keeps initialization separate (mermaid-init.js).
(function() {
  function load(src, onload, onerror) {
    var s = document.createElement('script');
    s.src = src;
    s.async = false;
    s.onload = onload;
    s.onerror = onerror;
    document.head.appendChild(s);
  }

  function tryLoadChain(sources, cb) {
    if (!sources || sources.length === 0) return cb(new Error('No sources'));
    var src = sources.shift();
    load(src, function() { cb(null, src); }, function() { tryLoadChain(sources, cb); });
  }

  var candidates = [
    'assets/js/mermaid.min.js', // relative (works on most static site setups)
    '/assets/js/mermaid.min.js', // absolute from site root
    'https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js'
  ];

  tryLoadChain(candidates.slice(), function(err, loaded) {
    if (err) {
      console.error('Failed to load mermaid from any source');
      return;
    }
    console.info('Mermaid loaded from', loaded);
    // mermaid-init.js will initialize mermaid (it waits for the global to be present)
  });
})();
