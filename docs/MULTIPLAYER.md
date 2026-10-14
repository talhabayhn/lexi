# Lexi — multiplayer design

| Mode | Release | Transport |
|---|---|---|
| Same-device duel (hot-seat) | v1.0 | `LocalTransport` |
| Online 1v1 Duel | v1.1 | `SupabaseRealtimeTransport` |
| Online Race (2–8 players) | v1.1 | `SupabaseRealtimeTransport` |
| Teams (2 teams, up to 8) | v1.2 | `SupabaseRealtimeTransport` |
| Async challenge link | v1.2 | `ChallengeLinkTransport` (+ results table) |
| Bluetooth / nearby | ✗ not planned | Android Nearby and iOS MultipeerConnectivity don't interoperate |

## 1. Core idea: deterministic lockstep
All clients run the same `GameEngine`. A match is fully defined by `MatchSetup` (seed, mode, levels, players,
rules version, contentVersion) + an ordered list of `MatchEvent`s. Clients only exchange events, never full state.
```kotlin
@Serializable data class MatchSetup(val matchId: String, val seed: Long, val mode: MatchMode,
    val players: List<PlayerSlot>, val rulesVersion: Int, val contentVersion: Int)
@Serializable data class MatchEvent(val seq: Long, val playerId: String, val atMs: Long, val action: PlayerAction)
```
- Each client keeps its own clock; `atMs` is relative to the match start broadcast by the host.
- `seq` is assigned by the host for Duel/Teams (turn-based, total order); Race has per-player sequences (no shared turn).
- Version guard: clients with a different `rulesVersion` or `contentVersion` cannot join (show "update the app").

## 2. Rooms (v1.1, Supabase)
- Anonymous Supabase Auth on first online use; user picks a nickname (validated, profanity-filtered).
- Room code: 5 chars from `ABCDEFGHJKMNPQRSTUVWXYZ23456789` (no 0/O/1/I/L). Table `rooms(code PK, host_id,
  setup jsonb, status, created_at, expires_at)`; RLS: read by code, write by host.
- Realtime channel `room:<code>`: **presence** (who is in the lobby, ready flags) + **broadcast** (MatchEvents).
- Host = creator. If the host disconnects mid-match for > 20 s, the next player by join order becomes host.
- Reconnect: client re-subscribes and requests the event log from the host (or from `room_events` table if we
  persist them) and replays.
- Creating a room: Pro only (or 1 free room per day). **Joining is always free.**
- Rooms expire after 2 hours (scheduled cleanup function).

## 3. Anti-cheat stance
Private friend rooms: low incentive, accepted risk. Answers are checked locally. If public matchmaking or
leaderboards ever ship, add a server-side validator (Edge Function replays the event log) before launch.

## 4. UI needed (design debt — draw before v1.1 implementation)
Online hub (create / join with code), lobby (players, ready, settings), duel in-match (own wheel + opponent
mini-wheel + whose turn), race in-match (progress bars of others), match result (podium), reconnect banner,
hot-seat handover card (v1.0 — build from components, flagged).

## 5. Async challenge (v1.2)
Link `https://<domain>/c/<id>` → app deep link. Payload: `MatchSetup` + challenger summary (score, time).
Result comparison stored in `challenges` table so the challenger gets notified. Share card reuses `12-share-card`.
