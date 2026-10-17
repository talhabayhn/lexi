# Lexi — project guide (Antigravity / Gemini)

All project rules, conventions, and workflow live in a single source of truth:

- **[CLAUDE.md](CLAUDE.md)** — read this first (stack, commands, rules, workflow, output discipline)
- **[docs/](docs/)** — ROADMAP, GAME_RULES, CONTENT, ARCHITECTURE, design/, ADRs, CONVENTIONS
- **[.claude/agents/](/.claude/agents/)** — specialised role definitions (see below)

Do not duplicate any rule here; follow CLAUDE.md and the docs it references.

## Specialised agents (`.claude/agents/`)

The role definitions under `.claude/agents/*.md` (architect, game-engine-engineer, ui-implementer,
data-engineer, platform-integrator, content-curator, test-engineer, code-reviewer,
multiplayer-engineer, debugger, chores) are Claude Code agent specs. In Antigravity, invoke them
as subagents: read the relevant `.md` file for the role's system prompt, tools, and scope, then
use `invoke_subagent` (or `/teamwork-preview`) with a prompt that includes those instructions.
