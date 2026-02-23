document.addEventListener('DOMContentLoaded', function() {
  if (typeof mermaid !== 'undefined') {
    try {
      mermaid.initialize({
        startOnLoad: true,
        theme: (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) ? 'dark' : 'default',
        securityLevel: 'loose',
        flowchart: { curve: 'basis' },
        sequence: { showSequenceNumbers: true }
      });
    } catch (e) {
      console.warn('Mermaid initialization failed:', e);
    }
  } else {
    // Retry if mermaid not yet loaded
    var t = setInterval(function() {
      if (typeof mermaid !== 'undefined') {
        clearInterval(t);
        try {
          mermaid.initialize({
            startOnLoad: true,
            theme: (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) ? 'dark' : 'default',
            securityLevel: 'loose',
            flowchart: { curve: 'basis' },
            sequence: { showSequenceNumbers: true }
          });
        } catch (e) {
          console.warn('Mermaid initialization failed:', e);
        }
      }
    }, 250);
  }
});