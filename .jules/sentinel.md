## 2024-10-29 - [Content Security Policy Implementation]
**Vulnerability:** Missing Content Security Policy (CSP) in offline-first Electron application allows data exfiltration if XSS occurs.
**Learning:** This application's architecture relies heavily on inline event handlers and styles. Therefore, a strictly ideal CSP (`script-src 'self'`) breaks functionality. We must allow `'unsafe-inline'` for `script-src` and `style-src`. However, since it is offline-first, we can completely lock down `connect-src` to `'self'` to mitigate exfiltration risks.
**Prevention:** Always include a baseline CSP tailored to the app's architectural constraints, restricting `connect-src` to `'self'` in offline apps.
