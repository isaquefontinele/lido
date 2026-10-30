# Roadmap

Total planned work to v1.0: **~100–125 hours**.

## Milestone 0 — Foundation (~8h)
- [ ] KMP + Compose Multiplatform skeleton, SQLDelight, Koin
- [ ] GitHub Actions: Android build, iOS framework build, shared tests

## Milestone 1 — Capture (~16h)
- [ ] Android share intent target
- [ ] iOS Share Extension (Swift) handing URLs to the shared module via App Group
- [ ] Fetch + extraction pipeline (Klead), metadata (title, author, site, lead image)
- [ ] Retry queue for links saved while offline

## Milestone 2 — Library (~12h)
- [ ] List with unread / archive / favorites
- [ ] Tags, sorting, swipe actions
- [ ] Full-text search (SQLite FTS)

## Milestone 3 — Reader (~16h)
- [ ] Sanitized HTML rendering of extracted content
- [ ] Typography controls and themes
- [ ] Offline images
- [ ] Reading progress and time estimate

## Milestone 4 — Highlights & notes (~14h)
- [ ] Text selection → highlight, robust anchoring (text quote + position)
- [ ] Notes on highlights
- [ ] Highlights screen per article and global; Markdown export

## Milestone 5 — Import / export (~10h)
- [ ] Pocket CSV export importer (tags, time added, status)
- [ ] Instapaper CSV and browser bookmarks HTML importers
- [ ] Full JSON + Markdown export

## Milestone 6 — Folder sync (~16h)
- [ ] Documented on-disk format
- [ ] Android: Storage Access Framework folder picker
- [ ] iOS: document picker / iCloud Drive container
- [ ] Merge rules and conflict tests

## Milestone 7 — Listen & release (~10h)
- [ ] Text-to-speech with platform voices, background playback
- [ ] pt-BR and English UI, accessibility pass
- [ ] TestFlight + Play internal testing

## Later
- Desktop app (Compose for Desktop)
- Browser extension that saves straight into the sync folder
- RSS/newsletter inbox
