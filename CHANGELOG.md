# Changelog

This file records the staged modernization of the FBLA Quiz Application. The Maven project currently identifies the artifact as version `1.0.0`, but Stage 9 does not create a Git tag or formal release; the documentation work therefore remains under `Unreleased`.

## Unreleased

### Stage 9 - Final documentation and release preparation

- Replaced the legacy run notes with a professional project README.
- Documented the features, technology stack, architecture, data flow, migration strategy, build, run, test, CI, repository layout, modernization highlights, and known limitations.
- Added a GitHub-renderable Mermaid architecture diagram and a branch-specific Maven CI status badge.
- Added this changelog without changing application behavior, quiz content, SQL migrations, database files, Git configuration, or workflow behavior.

## Modernization stages

### Stage 8 - Continuous integration and repository cleanup

- Added the Maven CI workflow for pushes and pull requests on `master` and `modernization`.
- Added Java 17 headless verification, runnable-JAR content checks, protected database hash checks, and short-lived build/report artifacts.
- Removed committed IDE metadata, compiled classes, database traces, and obsolete binary launch artifacts, then expanded ignore rules for generated files.

### Stage 7 - Quiz content audit and corrections

- Audited the question bank and recorded sources, reasoning, data checksums, and the one unresolved item in `docs/content-audit.md`.
- Preserved V2 as the historical import and added V3 for approved wording, answer, option, and identifier corrections.
- Added content-quality and migration integration checks for all 50 questions and 150 answer rows.

### Stage 6 - Swing UI modernization

- Replaced the monolithic legacy screen with application lifecycle, startup, controller, view, window, question-panel, and report components.
- Added focused presentations for four-button, drop-down, true/false, and fill-in-the-blank questions.
- Kept database loading off the Event Dispatch Thread and added regression tests for UI state, control flow, startup, and cleanup.

### Stage 5 - H2 runtime database migration

- Upgraded runtime persistence to H2 2.4.240.
- Introduced `DatabaseInitializer` and the V1 schema plus V2 preserved-content migrations.
- Moved normal runtime data to the ignored `data/` directory and added integration coverage for database creation and migration behavior.

### Stage 4 - Defect repair and startup handling

- Hardened repository validation and application-level database error handling.
- Corrected session edge cases and report-table mappings.
- Added asynchronous startup behavior and tests for repository failures, reports, and session validation.

### Stage 3 - Domain extraction and tests

- Introduced immutable `Question`, `QuizResult`, and `QuizReport` model objects.
- Added `QuestionRepository` to separate content access from quiz rules.
- Extracted `QuizSession` for distinct random selection, answer submission, scoring, and reporting, with unit tests.

### Stage 2 - Reproducible Maven build

- Reorganized Java sources and packaged resources under the standard Maven directory layout.
- Added the Java 17 Maven build, pinned Maven Wrapper, JUnit platform, and shaded runnable JAR configuration.
- Added generated-output ignore rules so a clean clone can build without committed compiler output.

### Stage 1 - Legacy database preservation

- Preserved the original H2 database as `database/backup/quizdb-original.mv.db` before persistence modernization.
- Documented the legacy schema and preservation controls in `database/SCHEMA.md`.
- Added a deterministic SQL export of the legacy quiz content for review and migration work.

## Major modernization outcomes

- A reproducible Java 17 build and self-contained runnable JAR.
- Testable separation among domain rules, persistence, application coordination, and Swing views.
- Transactional, versioned H2 initialization with immutable legacy preservation.
- Automated regression, migration, content-quality, UI, packaging, and CI safeguards.
- Auditable project documentation suitable for maintainers and reviewers.
