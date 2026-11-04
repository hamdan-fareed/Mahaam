## 2024-05-18 - [Content Security Policy (CSP) for Offline Apps]
**Vulnerability:** Missing Content Security Policy could allow data exfiltration if XSS occurs.
**Learning:** The offline-first architecture relies on inline event handlers (`unsafe-inline`), but restricting `connect-src` to `'self'` prevents exfiltration.
**Prevention:** Ensure strict CSP is added to all HTML entrypoints in offline-first apps, focusing on strict `connect-src` to prevent network requests.
