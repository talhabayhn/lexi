-- user.db schema version 3, frozen. Never edit: UserDatabaseMigrationTest migrates this snapshot to the current
-- schema. When adding migration <n>.sqm, also add snapshot <n+1>.sql (the schema after it).
CREATE TABLE progress(
    card_id TEXT NOT NULL PRIMARY KEY,
    box INTEGER,
    due_epoch_day INTEGER,
    seen INTEGER NOT NULL DEFAULT 0,
    correct INTEGER NOT NULL DEFAULT 0,
    wrong INTEGER NOT NULL DEFAULT 0,
    last_seen_ms INTEGER,
    mastered INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE session(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    mode TEXT NOT NULL,
    level TEXT NOT NULL,
    category TEXT,
    seed INTEGER NOT NULL,
    started_ms INTEGER NOT NULL,
    ended_ms INTEGER NOT NULL,
    epoch_day INTEGER NOT NULL,
    score INTEGER NOT NULL,
    correct INTEGER NOT NULL,
    wrong INTEGER NOT NULL,
    passed INTEGER NOT NULL,
    completed INTEGER NOT NULL
);
CREATE INDEX session_epoch_day ON session(epoch_day);
CREATE TABLE streak(
    epoch_day INTEGER NOT NULL PRIMARY KEY
);
CREATE TABLE daily_result(
    epoch_day INTEGER NOT NULL,
    level TEXT NOT NULL,
    score INTEGER NOT NULL,
    time_ms INTEGER NOT NULL,
    share_code TEXT,
    PRIMARY KEY (epoch_day, level)
);
CREATE TABLE joker(
    type TEXT NOT NULL PRIMARY KEY,
    count INTEGER NOT NULL CHECK(count >= 0)
);
CREATE TABLE joker_grant(
    kind TEXT NOT NULL PRIMARY KEY,
    epoch_day INTEGER NOT NULL
);
