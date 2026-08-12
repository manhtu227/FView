# Security Policy

## Supported versions

| Version | Support |
|---------|---------|
| 0.1.x   | Best effort |

## Reporting vulnerabilities

Please do **not** open a public issue for security-sensitive reports.

Contact the maintainer via GitHub: [manhtu227](https://github.com/manhtu227)

Include impact, repro steps, and whether the issue is in `:json-to-view` or the sample `:app`.

## Notes for integrators

- Apps are responsible for validating untrusted JSON from their own backends.  
- The library core is designed to render offline without fetching remote resources.  
