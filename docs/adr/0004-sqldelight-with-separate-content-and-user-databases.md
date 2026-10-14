# ADR 0004: SQLDelight with separate content and user databases

- Status: Accepted
- Date: 2026-10-14

## Context
Thousands of read-only cards ship with the app and will be updated; user progress must survive content updates.

## Decision
`content.db` built from CSV by tools:content-compiler and bundled; `user.db` created on device. Stable card slugs link them. Settings in DataStore.

## Consequences
Content updates are a file swap; no migrations for content. Two drivers to manage.
