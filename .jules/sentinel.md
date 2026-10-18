## 2024-10-18 - [Security Learning for Mahaam Desktop App]
**Vulnerability:** XSS from unsanitized date formatting & Data Exfiltration risk due to missing Content Security Policy in an offline Electron app.
**Learning:** Even though the app is meant to be offline, it was lacking a Content Security Policy (CSP). A missing CSP means that if an XSS vulnerability exists, data could be exfiltrated. We found XSS vulnerabilities in `src/app.js` where user input could bypass escaping if formatting functions were trusted blindly, for instance the `dueDate` which wasn't fully escaped.
**Prevention:**
1. Always enforce a strict `Content-Security-Policy` with `connect-src 'none'` for completely offline applications.
2. Ensure that even derived string concatenations based on user data are wrapped in sanitization functions like `escapeHtml()`.
