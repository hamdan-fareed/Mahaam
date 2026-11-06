## 2026-11-06 - [Missing Content Security Policy]
**Vulnerability:** The application was missing a Content-Security-Policy (CSP) in `src/index.html`.
**Learning:** In an offline-first Electron application with `nodeIntegration: false`, missing a CSP still leaves the application vulnerable to basic web-based attacks if malicious content is somehow loaded or injected. `connect-src 'self'` ensures the offline nature is strictly enforced.
**Prevention:** Always define a strict CSP for Electron apps, even if they are offline-first, to enforce resource loading constraints at the browser level.
