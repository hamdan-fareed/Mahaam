## 2024-05-24 - Content Security Policy Constraints
**Vulnerability:** Missing Content Security Policy (CSP) allowed potential data exfiltration and unauthorized script execution.
**Learning:** The application architecture heavily relies on inline event handlers (e.g., `onclick`) and inline styles, which necessitates the use of `'unsafe-inline'` for both `script-src` and `style-src` in its CSP. Additionally, being an offline-first app, restricting `connect-src` to `'self'` is critical to prevent any unintended data exfiltration to external servers.
**Prevention:** Always include a baseline CSP in `src/index.html` using a `<meta>` tag, customized for the app's specific architectural constraints (`unsafe-inline`) and offline-first nature (`connect-src 'self'`).
