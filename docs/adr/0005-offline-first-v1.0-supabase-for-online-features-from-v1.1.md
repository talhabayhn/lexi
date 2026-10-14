# ADR 0005: Offline-first v1.0; Supabase for online features from v1.1

- Status: Accepted
- Date: 2026-10-14

## Context
v1.0 must play without network; v1.1 adds private online rooms (duel, race) that need realtime presence/broadcast.

## Decision
No account or backend in v1.0. v1.1 uses Supabase (anonymous auth, Realtime presence + broadcast, Postgres with RLS, Edge Functions) via the official supabase-kt KMP client.

## Consequences
Lowest ops burden; official KMP SDK; free tier for launch. Vendor lock-in limited by MatchTransport abstraction.
