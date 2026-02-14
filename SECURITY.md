# Security Policy

This project uses API keys and CI/CD workflows. Follow these rules to keep secrets safe.

## Local Secrets (Development)
- Store secrets in a local `.env` file.
- Never commit `.env` to git.
- Use `.env.example` as the template for required variables.

## GitHub Secrets (CI/CD)
- Add secrets in GitHub: Settings → Secrets and variables → Actions.
- Required secret name: `API_FOOTBALL_KEY`.
- The workflow reads secrets from `${{ secrets.API_FOOTBALL_KEY }}`.

## API Key Rotation
- Rotate API keys every 90 days.
- If a key leaks, rotate immediately and revoke the old key.

## Reporting Vulnerabilities
- Please report security issues via GitHub Issues or direct contact with the maintainer.

## Branch Protection Rules
Configure in GitHub: Settings → Branches → Branch protection rules.
Recommended for `main`:
- Require pull request before merging
- Require 1 approval
- Require status checks to pass
- Do not allow force pushes
- Do not allow branch deletion

## Pre-commit Secret Scan
This repo includes a local pre-commit hook to catch obvious secret leaks.
Enable it once per clone:

```bash
git config core.hooksPath .githooks
```
