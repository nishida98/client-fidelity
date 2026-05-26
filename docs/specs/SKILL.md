---
name: spec-driven-build
description: "Generates a detailed technical specification and a structured implementation plan for fixes or new features."
version: 1.0
---

## When to use

Use this command to generate a detailed technical specification and a structured implementation plan for any fix or new feature. The output will be a complete SDD (Software Design Document) and a list of granular tasks, both aligned with the project standard.

## Role

You are a senior software engineer responsible for producing detailed technical specifications according to the project standard. When the user invokes `/spec`, follow **exactly** the flow below.

---

## PHASE 0 — Context gathering

Before writing any file, ask the necessary questions to understand:

1. **What needs to be fixed or implemented?** (free-form description of the problem or feature)
2. **What is the impact or risk if this is not done?** (e.g., legal violation, critical bug, technical debt)
3. **Which files or areas of the code have you already identified as affected?** (can be "I don't know")
4. **Are there any constraints or decisions already made?** (e.g., "we cannot use library X", "must follow pattern Y")

If the user already provided the context in the command invocation (e.g., `/spec Add 2FA authentication`), skip the questions whose answers are already evident. Ask only the questions that will actually improve the specification.

Before proceeding, **always confirm the feature name** that will be used as the folder name:
- Format: kebab-case, descriptive, and short (e.g., `autenticacao-2fa`, `lgpd-cookie-consent`, `export-csv`)
- Confirm: _"I will create the files in `docs/<name>/`. Is that correct?"_

---

## PHASE 1 — SDD.md generation

Explore the relevant code using the available tools (Read, Grep, Glob) to ground the specification with real references: file names, line numbers, TypeScript interfaces, function names.

Create the file at `docs/<feature-name>/SDD.md` strictly following this structure:

```markdown
# SDD — <Feature or Fix Title>

## 1. Context and Problem

<Clear description of the current state. Reference specific files and lines in the code.
Include the risk or motivation (legal, technical, UX). Maximum 3 paragraphs.>

---

## 2. Scope of the Fix

### 2.1 What changes

| Area | Current situation | Target situation |
|---|---|---|
| <component/file> | <what exists today> | <what will exist after the change> |

### 2.2 What does not change

- <explicitly list what is out of scope>

---

## 3. Solution Design

### 3.1 <Name of the first component/module>

<Technical description with TypeScript/TSX code snippets ready to use.
Reference the exact line in the file when modifying existing code.>

### 3.2 <Next component, if any>

<...continue for each part of the solution...>

---

## 4. Flow after the fix

\`\`\`
<Text diagram (ASCII) showing the user or data flow after the implementation>
\`\`\`

---

## 5. Files to modify/create

| File | Type of change |
|---|---|
| `src/...` | Modify — <concise description> |
| `src/...` | **Create** — <concise description> |

---

## 6. Acceptance Criteria

- [ ] <objective and verifiable criterion>
- [ ] <objective and verifiable criterion>
- [ ] `bun run build` passes without errors.

---

## 7. Additional considerations

<Notes about security, performance, accessibility, future technical debt, or external dependencies. Omit this section if there is nothing relevant.>
```

### SDD quality rules

- **Code snippets**: always complete and compilable. Never use `// ...` inside a snippet that will be copied directly — use `// rest of the code unchanged` only in context blocks.
- **References to current code**: include the line number when modifying existing code (e.g., `line ~514`).
- **No ambiguity**: each solution section must be implementable without requiring assumptions.
- **Surgical scope**: do not propose refactors beyond what is necessary. If something is out of scope, state it explicitly in section 2.2.

---

## PHASE 2 — SDD validation

After generating the SDD, perform a self-review before proceeding. Check:

- [ ] Do all files mentioned in section 5 exist in the project, or are they explicitly new?
- [ ] Are the code snippets compatible with the library versions used in the project?
- [ ] Are the acceptance criteria objectively verifiable?
- [ ] Is the scope clear, with no ambiguity between what changes and what does not change?

If you find inconsistencies, fix the SDD before proceeding.

Present the user with a 3–5 line summary of the generated SDD and ask: _"Is the SDD correct? Can I generate the tasks?"_

---

## PHASE 3 — Task generation

After the SDD is approved, create the task files in `docs/<feature-name>/tasks/`.

### File naming

```
TASK-1-<description-in-kebab-case>.md
TASK-2-<description-in-kebab-case>.md
...
TASK-N-verificacao-e2e.md   ← always the final task
```

### Structure of each task file

```markdown
# TASK-N — <Clear description of what this task does>

**Target file:** `src/...` (new | existing)
**SDD reference:** Section X.Y
**Depends on:** TASK-X, TASK-Y | none
**Blocked by:** TASK-X | none

---

## Context

<Why this task exists. What the current code does incorrectly or what is missing.
Reference the exact file and line when relevant. 2–4 sentences.>

## What to do

<Specific instructions with code snippets ready to copy.
Include the "before" and "after" when modifying existing code.>

## Implementation notes

<Pitfalls, non-obvious decisions, type dependencies, expected side effects.
Omit if there is nothing relevant.>

## Acceptance criteria

- [ ] <objective verification>
- [ ] `bun run build` passes without errors.
```

### Task rules

1. **Granularity**: each task must be executable independently or with explicit dependencies. One task = one modified or created file, except when the change is trivially small and indivisible.
2. **Explicit parallelism**: if tasks can be done in parallel, indicate `Depends on: none` in both.
3. **Dependency order**: tasks must be executable in numerical order without blocking each other, except where explicitly indicated.
4. **Mandatory final task**: the last task is always `TASK-N-verificacao-e2e.md` with a complete checklist of all SDD acceptance criteria.
5. **Complete snippets**: follow the same standard as the SDD — snippets ready to use, without pseudocode.
6. **No "documentation" task**: the documentation is the SDD itself. Tasks are exclusively for code.

### Number of tasks

Derive from section "5. Files to modify/create" of the SDD:
- 1 significant new or modified file = 1 task, generally
- Multiple small changes in the same file = 1 task
- Always add the final e2e verification task

---

## PHASE 4 — Final summary

After creating all files, present:

```
✓ SDD:   docs/<feature>/SDD.md
✓ Tasks: docs/<feature>/tasks/
   TASK-1 — <description>         [independent]
   TASK-2 — <description>         [after TASK-1]
   TASK-3 — <description>         [after TASK-1, parallel with TASK-2]
   ...
   TASK-N — E2E verification      [final]

Parallel: TASK-X and TASK-Y can be executed simultaneously.
```

---

## Pattern references

The reference SDDs for this project are located at:
- `docs/lgpd-consent-terms/SDD.md` — example of a fix involving multiple files and new routes
- `docs/lgpd-cookie-consent/SDD.md` — example of a fix involving a new module, component, and guard

The reference tasks are located in the respective `tasks/` folders of each SDD above.