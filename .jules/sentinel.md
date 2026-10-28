## 2024-05-18 - Missing Content Security Policy (CSP)
**Vulnerability:** The offline-first Electron app lacked a Content Security Policy (CSP), specifically leaving `connect-src` unrestricted, which could allow data exfiltration if XSS were ever achieved.
**Learning:** Even fully offline local applications need strict network boundaries (`connect-src 'self'`) because XSS could otherwise still send data out to external servers. Because of the vanilla JS architecture, `'unsafe-inline'` is required for scripts/styles, making CSP even more critical to limit blast radius.
**Prevention:** Always add a strict CSP to Electron HTML files restricting `connect-src`, `default-src`, and other directives to `'self'` or explicit domains.
