# Execution Plan — AI Sales CRM Agent

> Rules for using this file:
> - Do ONE step at a time. Don't jump ahead.
> - Each step should be small enough to build + manually verify in one sitting.
> - Check a step off only after it runs and you've verified it (curl/Postman/test), not just "code compiles."
> - Refer to `master.md` for architecture rules — don't re-decide them mid-step.
> - If a step reveals a new decision, add it to master.md §11 before continuing.

Legend: `[ ]` not started · `[~]` in progress · `[x]` done

---

## STAGE 0 — Project Skeleton

- [x] 0.1 Init Spring Boot project (Java 21, Web, Security, Data JPA, Validation, Flyway) — no business logic yet. Confirm it boots.
- [ ] 0.2 Create a dedicated local DB + user in your existing Postgres 16 instance (e.g. `salescrm` db, `salescrm_app` role) — don't reuse a shared/default DB. Confirm you can `psql` into it.
- [ ] 0.3 Install pgvector extension into your local Postgres 16 (`CREATE EXTENSION vector;` on the `salescrm` db). Confirm `\dx` shows it installed — this step blocks Stage 6 later, so verify it now even though it's unused until then.
- [ ] 0.4 Connect Spring Boot to local Postgres via `application.yml` (or `application-local.yml`). Confirm app boots against it.
- [ ] 0.5 Add Flyway, create empty baseline migration. Confirm migration runs on boot.
- [x] 0.6 Package structure: create empty module folders per master.md §7 module list (auth, organization, user, lead, contact, company, deal, pipeline, activity, task, note, ai, audit, common). No code inside yet — just skeleton.

## STAGE 0a — Shared Conventions Scaffolding (mirrors reference project's `resources`/`entities`/`security` packages — master.md §7a)

Build these once, before any real entity, so every module built afterward follows the same pattern from the start instead of retrofitting it later.

- [x] 0a.1 `common/entity/BaseEntity.java` — id field, `isNew()`, per master.md §7a.
- [x] 0a.2 `common/resources/Constants.java` — status-type strings + `|ENTITY|`-templated messages (start with the ones you know you'll need: LIST/DETAIL/SAVE/UPDATE/DELETE success+failure, EXECUTION_ERROR, PARAMETER_MISSING, INVALID_TOKEN). Add more as later stages need them rather than guessing all of them now.
- [x] 0a.3 `common/resources/LogConstants.java` — entity name + action constants, same pattern, start with LEAD/CONTACT/COMPANY/DEAL/TASK/NOTE/ACTIVITY + LIST/ADD/EDIT/DELETE.
- [x] 0a.4 `common/resources/ApiStatus.java` — the shared response wrapper (statusType, text, token, total, + one field per entity as entities get built — start with just the scalar fields, add entity fields incrementally per Stage 2 step).
- [x] 0a.5 `common/resources/Resources.java` — static helpers: `setStatus(type, text, entity)` with token replacement, `formatedResponse(obj, properties)` using `MappingJacksonValue` + `@JsonFilter`, `getDefaultRequest(request)` for pagination defaults. Confirm with one throwaway controller endpoint that filtering actually restricts the JSON output.
- [x] 0a.6 Confirm decision #7 (master.md §11) before this step: thin `@RestControllerAdvice` safety net — build it now (malformed JSON body, 404 route not found, security-filter auth failures only), not as the primary error path.
- [x] 0a.7 Confirm decision #8 (master.md §11) before this step: `common/security/TenantContextService.java` — `getCurrentContext()` returning `{userId, organizationId, role}`, backed by `SecurityContext` per the default assumption in master.md, exposed in the same shape every service method will call.

## STAGE 1 — Auth & Multi-Tenancy Core

- [ ] 1.1 `organizations` + `users` tables via Flyway migration. Fields per master.md §8 (expand fields at this step, not before). Include `is_active`/`is_deleted`/`created_on`/`updated_on` on both, per the soft-delete convention (master.md §11 decision #11).
- [ ] 1.1a `user_types` table + entity (enum-backed: `ORG_ADMIN`/`SALES_MANAGER`/`SALES_REP`), `users.user_type_id` FK (many-to-one) — mirrors reference's `UserType`. Seed the three rows via migration.
- [ ] 1.1b `roles` table + entity (enum-backed: `ROLE_ORG_ADMIN`/`ROLE_SALES_MANAGER`/`ROLE_SALES_REP`) + `user_roles` join table (many-to-many) — mirrors reference's `Role`/`ERole`/`user_roles`. Seed the three rows via migration.
- [ ] 1.2 `POST /auth/register` — creates User + Organization atomically, first user = ORG_ADMIN. Look up and assign both `UserType` and default `Role` the same way reference does (repository lookup by enum name, not a shortcut single-field assignment). Test with curl.
- [ ] 1.3 Password hashing (BCrypt) wired in.
- [ ] 1.4 JWT issuing on register/login (access token only, no refresh yet). Confirm token decodes with correct claims (userId, organizationId, role).
- [ ] 1.5 `POST /auth/login`. Test with curl.
- [ ] 1.6 Refresh token: table + `POST /auth/refresh` + rotation on use.
- [ ] 1.7 Spring Security filter chain wired to validate JWT on protected routes. Confirm a protected test endpoint returns 401 without token, 200 with valid token.
- [ ] 1.8 **Tenant isolation enforcement mechanism** (master.md rule #1) — build the base repository / Hibernate filter that auto-injects `organization_id`. Prove it with a throwaway test entity before building real entities on top of it. This is the most important step in the whole project — do not skip or rush it.
- [ ] 1.9 RBAC: role-based method security annotations (`@PreAuthorize`) wired and tested on one dummy endpoint per role.
- [ ] 1.10 Rework 1.2–1.6 to return `ApiStatus` (not raw DTOs/exceptions) and use `TenantContextService` + `Constants`/`LogConstants` from Stage 0a, so auth module is the first real example of the shared pattern before it's copy-pasted across Stage 2.
- [ ] 1.11 `user_logs` table + entity (mirrors reference's `UserLog`: `action`, `sub_action`, `created_on`, `updated_on`, `is_active`, `is_deleted`, FK to `users`).
- [ ] 1.12 `LogService`/`LogServiceImpl` — `logs` (paginated list), `viewLog`, `addLog`, `editLog`, `deleteLog` (soft-delete), and a `createLog(user, action, subAction, createdOn, updatedOn)` helper for other services to call directly — same shape as reference's `LogService`.
- [ ] 1.13 Wire `logService.createLog(...)` into register/login (`LogConstants.USER` + `SIGN_IN`/`SIGN_UP`-equivalent actions), matching how reference calls it after auth events. This is the pattern every later module's mutating endpoints will follow too (log call right after the `ApiStatus` success branch, not a separate audit step) — confirm this feels right before repeating it across Stage 2, since it's a lot of near-duplicate log calls to write by hand.

## STAGE 2 — Core CRM Entities (repeat pattern per entity, smallest first)

Do these one at a time, each fully CRUD + tested before starting the next. **Every entity follows the Stage 0a/Stage 1 pattern exactly**: entity extends `BaseEntity` + carries `is_active`/`is_deleted`/`created_on`/`updated_on` (soft-delete convention, master.md §11 #11), service methods return `ApiStatus`, try/catch per method (no throwing), `Constants`/`LogConstants` for messages/logging, `TenantContextService` for the current user/org, a `logService.createLog(...)` call after each successful mutation (mirrors 1.13), controller stays thin and calls `Resources.formatedResponse(status, properties)`. Don't deviate per-entity — that consistency is the point of Stage 0a/1.

- [ ] 2.1 `companies` — entity, migration, repository, service, controller, CRUD endpoints, pagination (list query filters `is_deleted=false`). Test create/list/get/update/delete via curl — delete should soft-delete, confirm the row still exists in the DB with `is_deleted=true`. Add `company`/`companies` fields to `ApiStatus` as part of this step (per master.md §7a — grow the wrapper incrementally, not upfront).
- [ ] 2.2 `contacts` (FK to companies) — same pattern.
- [ ] 2.3 `leads` — same pattern + status lifecycle enforcement (NEW→CONTACTED→QUALIFIED→CONVERTED/LOST, invalid transition = 422).
- [ ] 2.4 `POST /leads/{id}/convert` — creates linked Contact/Company/Deal in one transaction.
- [ ] 2.5 `pipelines` + `pipeline_stages` — CRUD, seed a default pipeline.
- [ ] 2.6 `deals` — CRUD + `POST /deals/{id}/move-stage`.
- [ ] 2.7 `activities` — CRUD, linkable to lead/contact/company/deal.
- [ ] 2.8 `tasks` — CRUD, status + priority enums.
- [ ] 2.9 `notes` — CRUD, polymorphic entity_type/entity_id per master.md §8 flag.
- [ ] 2.10 ~~Global exception handler~~ Already built in Stage 0a.6 as a thin safety-net only — nothing to do here except confirm it hasn't silently become the primary error path (spot-check a few endpoints: errors should come back as `ApiStatus{STATUS_ERROR}` from the service layer, not as the advice's generic error shape).
- [ ] 2.11 Cross-tenant isolation test: two orgs, prove org A can't fetch org B's lead/deal/contact/company by ID (expect 404, not 403).

## STAGE 3 — Audit Log (do this before AI, so AI actions are covered from day one)

- [ ] 3.1 `audit_logs` table + write-path helper (source: MANUAL/AI/API/SYSTEM).
- [ ] 3.2 Wire audit logging into every mutating endpoint built so far (leads, deals, contacts, companies, tasks, notes, stage-move).
- [ ] 3.3 `GET /audit-logs` (paginated, org-scoped) to visually confirm entries are landing correctly.

## STAGE 4 — Customer 360 (pure CRM, no AI yet)

- [ ] 4.1 `GET /customers/{id}/360` — aggregate company + contacts + leads + deals + notes + activities into one payload. No AI summary yet, just the aggregation.

## STAGE 5 — AI Agent Foundation

> Reminder (master.md §7a): AI/chat/RAG responses are the deliberate exception to the `ApiStatus` pattern. Use dedicated DTOs (`ChatResponse`, etc.) here, not the shared wrapper. Everything else (tool implementations calling into Stage 2 services) still goes through `ApiStatus` underneath.

- [ ] 5.1 Add Spring AI dependency, wire `LLMProvider` interface + `OpenAIProvider` implementation (master.md #1 — Azure stub only, not implemented). Confirm a raw "hello" completion works via a throwaway test endpoint.
- [ ] 5.2 `conversations` + `conversation_messages` tables. `POST /ai/chat` that just echoes LLM text response (no tools yet), returning a dedicated `ChatResponse` DTO — not `ApiStatus`. Confirm round-trip works and messages persist.
- [ ] 5.3 `tool_executions` table (schema only, not wired yet).
- [ ] 5.4 Build ONE tool end-to-end first: `searchLeads()`. Wire tool-calling through Spring AI, confirm the agent calls the real LeadService (not repository directly — master.md rule #2), and logs to `tool_executions`.
- [ ] 5.5 Verify architecture rule #2 explicitly: write a short note/test confirming the tool path goes through the service+authorization layer, not a shortcut.
- [ ] 5.6 Add 3-4 more read-only tools: `getLead`, `searchDeals`, `getDeal`, `getCustomerTimeline`. Test each via chat prompts.
- [ ] 5.7 Add write tools: `createTask`, `updateDealStage`. Confirm audit_logs entries show `source: AI_AGENT`.
- [ ] 5.8 Destructive-action confirmation flow (master.md rule #4) — implement conversation-state tracking for pending confirmations. Test with a bulk-delete-style prompt (even if the actual destructive tool is just a stub for now).
- [ ] 5.9 System prompt hardening (master.md rule #3) — write the system prompt treating retrieved data as non-instructional. Create ONE seeded note containing an injection attempt ("ignore previous instructions...") and write a test proving the agent doesn't act on it.
- [ ] 5.10 Customer 360 AI summary — `POST /ai/chat` intent that uses the Stage 4 aggregation as tool output and returns a natural-language summary.

## STAGE 6 — RAG

- [ ] 6.1 `documents` + `document_chunks` (pgvector column) tables.
- [ ] 6.2 Document upload endpoint (PDF/DOCX/TXT) — text extraction only, no embedding yet. Confirm text comes out correctly for each file type.
- [ ] 6.3 Chunking (fixed ~500 tokens, overlap — master.md #2, revisit later if results are poor).
- [ ] 6.4 Embedding generation + storage in `document_chunks` with `organization_id`.
- [ ] 6.5 Retrieval: similarity search filtered by `organization_id` FIRST (master.md rule #5), then similarity ranking. Write a cross-tenant test proving org A never gets org B's chunks back, even for a highly similar query.
- [ ] 6.6 Wire retrieval into `/ai/chat` as a tool/context source. Confirm chat responses cite which document was used.

## STAGE 7 — Demo Polish & Verification

- [ ] 7.1 Seed script: sample org, ~20 leads, contacts, companies, a pipeline, deals, activities, 1-2 sample documents.
- [ ] 7.2 Run the full Killer Demo Flow (master.md §10) end-to-end, no manual DB fixes.
- [ ] 7.3 Manual eval: run ~15 test queries, log correct/incorrect tool selection, target ≥90% (master.md §9).
- [ ] 7.4 Record the demo video / walkthrough.
- [ ] 7.5 Write README — setup steps must cover: local Postgres 16 + pgvector prerequisite (not Docker), how to create the DB/role, how to run migrations, architecture diagram, demo instructions, what's MVP vs deferred per master.md §5.
- [ ] 7.6 (Optional, only if you want it) Dockerize the Spring Boot app itself for portfolio polish — Postgres stays local/host, not containerized. Skip entirely if not needed.

---

## Deferred (v2/later — do NOT start until all of Stage 0-7 above is done)

Redis, rate limiting, lead scoring, scheduled deal-risk jobs, AI cost tracking, observability stack, real email sending, Kafka, integrations, Temporal, formal eval harness, Azure OpenAI live implementation. (Full list: master.md §5.)

---

**Next action:** Start Stage 0, Step 0.1. Report back after each step (or small batch of steps) rather than building ahead — this file is meant to keep the work incremental.
