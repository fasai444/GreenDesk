If you want to bundle Mermaid locally for offline builds / CI without CDN,
download the minified Mermaid distribution and place it at:

- `GreenDesk-Doc/assets/js/mermaid.min.js`

You can get the file from the official CDN, for example:

```
curl -L -o GreenDesk-Doc/assets/js/mermaid.min.js \
  https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js
```

The site already includes `mermaid-bundle.js` which will try to load:

1. `assets/js/mermaid.min.js` (relative)
2. `/assets/js/mermaid.min.js` (absolute)
3. CDN fallback `https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js`

After placing the file locally, a fully offline build will use the local copy.
