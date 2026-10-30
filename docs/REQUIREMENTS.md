# Requirements — Lido

## 1. Goal
An open-source, local-first read-it-later app for Android and iOS that works fully offline and keeps the user's library in open, portable files.

## 2. Functional requirements

| ID | Requirement |
|---|---|
| FR-01 | Save URLs from the system share sheet (iOS Share Extension, Android share intent) and by pasting. |
| FR-02 | Fetch the page and extract the main article content and metadata. |
| FR-03 | Store text and images locally for offline reading. |
| FR-04 | Reader view with font, size, spacing and theme controls. |
| FR-05 | Library with unread, archive, favorites, tags and sorting. |
| FR-06 | On-device full-text search. |
| FR-07 | Create highlights and notes; export them to Markdown. |
| FR-08 | Track reading progress and show reading-time estimates. |
| FR-09 | Import Pocket CSV exports, Instapaper CSV and browser bookmark HTML. |
| FR-10 | Export the full library to JSON + Markdown. |
| FR-11 | Optional sync through a user-chosen folder using a documented file format. |
| FR-12 | Text-to-speech using the platform's built-in voices. |
| FR-13 | UI in Portuguese (pt-BR) and English. |

## 3. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-01 | No project-owned server, accounts, analytics or trackers. |
| NFR-02 | Network access only to fetch pages the user saved. |
| NFR-03 | Saved content is never redistributed; sharing an item shares only its original URL. |
| NFR-04 | Extracted HTML is sanitized (no scripts, no remote resources at read time). |
| NFR-05 | Sync format is versioned and documented; merges are deterministic and tested. |
| NFR-06 | Library of 5,000 items opens in < 1 s; search returns in < 200 ms. |
| NFR-07 | Accessibility: screen readers, dynamic type, high-contrast themes. |

## 4. Out of scope
- Hosted sync service or web app.
- Social features, recommendations or public profiles.
- Paywall bypassing; Lido saves only what the user's request can normally access.

## 5. Acceptance criteria for v1.0
- Save → read offline → highlight → export works on both platforms.
- Pocket CSV import tested with synthetic export files.
- Two devices syncing through the same folder converge without data loss in the test suite.
