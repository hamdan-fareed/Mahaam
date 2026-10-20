## 2025-05-18 - [Missing Content Security Policy in offline-first architecture]
**Vulnerability:** Missing Content Security Policy (CSP) tag in `src/index.html`.
**Learning:** This offline-first Electron-based application was missing a Strict Content Security Policy (CSP), which could potentially allow data exfiltration to external servers if XSS were to be exploited.
**Prevention:** A strict CSP (`<meta http-equiv="Content-Security-Policy" content="default-src 'self'; connect-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' data:;">`) was added to `src/index.html` to prevent this class of attacks by strictly limiting where the application can load resources from and send data to.
