# MCFootball

> A MontiCore-based football website generator.  
> Write match data in a `.fb` DSL — get a static HTML site for GitHub Pages.

**MontiCore 7.7.0-SNAPSHOT** · **Gradle build** · **Open Source**

---

## What This Project Does

MCFootball uses the [MontiCore](https://monticore.de/) language workbench to
define a domain-specific language (DSL) for football match data. You write
`.fb` model files describing countries, leagues, and matches — MontiCore
generates a parser, and a custom generator turns the parsed AST into a static
HTML website served by GitHub Pages.

```
.fb model → MontiCore parser → AST → FreeMarker templates → static HTML → GitHub Pages
```

---

## For New Contributors

### Where to start

1. **Read [`DESIGN.md`](DESIGN.md)** — the design source of truth.
   It covers the grammar, project structure, build pipeline, and 6-phase roadmap.
   Every section has an inline diagram for visual context.

2. **Browse the charts** — see [`docs/charts/CHART-MAP.md`](docs/charts/CHART-MAP.md)
   for the full diagram index with descriptions and markdown snippets.

3. **Understand the migration** — see [`MIGRATION.md`](MIGRATION.md)
   for how the repo is being restructured from flat files to the target Gradle layout.

### What is legacy

All files in `legacy/` are from the v0 prototype. They are kept for reference
but are **not part of the new build**. The original code used Jsoup scraping
instead of MontiCore — the redesign replaces that with a proper DSL pipeline.

Key v0 insight: [`legacy/FootballResultsTest.java`](legacy/FootballResultsTest.java)
already imports MontiCore's generated parser — proof the project was always
intended to use MontiCore.

### What the new directories are for

| Directory | Purpose | Status |
|---|---|---|
| `src/main/grammars/football/` | MontiCore `.mc4` grammar | Waiting for Phase 1 |
| `src/main/java/football/` | Tool, generator, CoCos | Waiting for Phase 1 |
| `src/main/resources/templates/` | FreeMarker `.ftl` templates | Waiting for Phase 3 |
| `src/test/resources/football/valid/` | Valid `.fb` test models | Waiting for Phase 2 |
| `src/test/resources/football/invalid/` | Invalid `.fb` for CoCo tests | Waiting for Phase 4 |
| `output/` | Generated HTML (gitignored) | Built by Gradle |
| `docs/charts/` | Project diagrams | ✅ Available |

### Key design decisions

- **Grammar name:** `FootballSite` (file: `FootballSite.mc4`)
- **Model file extension:** `.fb`
- **MontiCore version:** 7.7.0-SNAPSHOT
- **Build system:** Gradle with MontiCore plugin
- **Blueprint:** SLE-lite `website/` example (see DESIGN.md §3 for the mapping)
- **Phase 1 scope:** 3 leagues (Bundesliga, Premier League, La Liga)

### Phase 1 Decisions (Locked)

These paths and names are frozen — all code must follow them:

| What | Exact path / name |
|---|---|
| Grammar | `src/main/grammars/football/FootballSite.mc4` |
| Entrypoint | `football.FootballSiteTool` → `src/main/java/football/FootballSiteTool.java` |
| Generator | `football.generator.FootballSiteGenerator` → `src/main/java/football/generator/FootballSiteGenerator.java` |
| CoCo registry | `football.cocos.FootballSiteCoCos` → `src/main/java/football/cocos/FootballSiteCoCos.java` |
| Build config | `build.gradle` + `settings.gradle` + `gradle.properties` at repo root |
| MC version | `mc_version=7.7.0-SNAPSHOT` in `gradle.properties` |
| Java target | 11 |

---

## Project Structure

```
MCFootball/
├── DESIGN.md          # Design source of truth
├── MIGRATION.md       # Repo restructuring plan
├── README.md          # This file
├── docs/charts/       # 12 project diagrams + CHART-MAP.md
├── legacy/            # v0 prototype files (reference only)
├── src/main/          # Grammar, Java, templates (Phase 1+)
├── src/test/          # Tests and .fb model fixtures
└── output/            # Generated HTML (gitignored)
```

---

## Status

**Current phase: Pre-Phase 1 (Documentation & Restructuring)**

The repo is being restructured from a flat file layout to a proper Gradle
project. No Java or grammar code has been written yet. See DESIGN.md §6
for the full roadmap.

---

## License

Open Source — see [LICENSE](LICENSE) (to be added).

---

*Author: ömer3kale*
