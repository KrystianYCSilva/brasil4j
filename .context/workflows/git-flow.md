# Git flow and delivery (VII)

## Branches

| Branch | From | Merges into | Purpose |
| --- | --- | --- | --- |
| `main` | — | — | Released code only; every commit is tagged `vX.Y.Z` |
| `develop` | `main` | — | Integration; version is the next `X.Y.Z-SNAPSHOT` |
| `feature/NNN-name` | `develop` | `develop` (PR) | One spec or change (`NNN` = spec number) |
| `release/X.Y.Z` | `develop` | `main` (PR), then back to `develop` | Freeze, version bump, CHANGELOG |
| `hotfix/X.Y.Z` | `main` | `main` (PR), then `develop` | Urgent fix on a release |

## Rules

- **GF-1** No direct push to `main` or `develop`; rulesets in `.github/rulesets/` require a PR, all required checks green, an up-to-date branch and resolved conversations. No bypass, including admins.
- **GF-2** Required checks: `verify (8)`, `verify (21)`, `api-compatibility`, `mutation`; PRs into `main` also need `branch-policy`.
- **GF-3** `main` only receives PRs from `release/*` or `hotfix/*`.
- **GF-4** A release: create `release/X.Y.Z` from `develop` → set version `X.Y.Z` → CHANGELOG section → PR to `main` → merge → tag `vX.Y.Z` on `main` → `release.yml` publishes the GitHub Release → back-merge through `chore/back-merge-X.Y.Z` (from `develop`, merge `main` into it, set the next SNAPSHOT) → PR into `develop`. After a minor/major release, `develop` becomes `X.Y+1.0-SNAPSHOT`; after a hotfix `X.Y.Z+1`, the back-merge keeps `develop`'s existing SNAPSHOT version. `release/*` and `hotfix/*` into `main` AND the `chore/back-merge-X.Y.Z` PR into `develop` always use merge commits (never squash) to keep ancestry; feature PRs into `develop` may squash. Delete those branches manually after the back-merge.
- **GF-5** Tags `v*` are protected; never move or delete a published tag.
- **GF-6** Repository settings (default branch `develop`, no automatic branch deletion, auto-merge, no rebase merge) and rulesets are applied with `pwsh scripts/github/apply-repo-settings.ps1 -Apply` (dry run without `-Apply`); edit the JSON, never the GitHub UI.

## Versions

Released: `v0.1.0`, `v0.2.0`. In progress: `0.3.0` (Specs 006–011 and 014) on `develop` as `0.3.0-SNAPSHOT`. Maven Central publication is pending namespace approval; until then releases are tags plus GitHub Releases only, and tag adjustments may be needed when publication starts.

## Commits

Conventional commits (`feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`) with scope (`feat(identidade): …`). One spec step per commit.
