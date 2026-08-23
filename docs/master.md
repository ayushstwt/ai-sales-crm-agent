# AI Sales CRM Agent — Master Doc

> Source of truth for this project. `plans.md` breaks this into small, sequential build steps.
> Update this file whenever a scope/architecture decision changes. Do not duplicate detail into plans.md — plans.md only references sections here.

---

## 1. Problem Statement

Sales reps/freelancers waste time manually cross-referencing scattered CRM data to answer basic questions ("who needs follow-up," "which deals are at risk") and manually summarizing customer context before calls.

Secondary goal: portfolio differentiation. Generic CRUD-CRM clones are common. This project demonstrates **multi-tenant backend engineering + AI agent design** (tool calling, RAG, tenant-safe retrieval, prompt-injection defense) together — not just CRUD.

## 2. Target User

Portfolio/demo project. Optimized for defensible engineering over real-user polish. No deployment required — local Docker Compose demo is the deliverable.

Personas (for grounding demo scenarios only):
- **Rahul** — freelance consultant, 40 leads, 15 deals, wants "what do I need to do today."
- **Priya** — sales manager, 6-person team, wants deal-risk visibility and AI summaries before reviews.

## 3. Goals / Non-Goals

**Goals**
- Full multi-tenant CRM CRUD (leads, contacts, companies, deals, pipeline, activities, tasks, notes)
- AI chat agent with real tool calling against the actual service layer
- RAG over uploaded documents, tenant-isolated at the embedding level
- Demonstrable prompt-injection defense
- One clean, recordable demo flow

**Non-Goals (MVP)**
- Deployment (local Docker Compose only)
- Redis, rate limiting, Kafka, observability stack (Prometheus/Grafana)
- Real email sending (drafts only)
- Multi-provider runtime switching (abstraction exists; only one provider wired)
- Fine-grained field-level permissions

## 4. Roles

`ORG_ADMIN`, `SALES_MANAGER`, `SALES_REP` — mirrored from reference as a dual model, not a single field: a `UserType` (single, business-category FK on `users`) plus `Role`/`user_roles` (many-to-many, drives Spring Security authorities). See §8 for the entity breakdown.

## 5. MVP Feature Scope (Phase 1 + 2 + 3 from original brainstorm)

**Phase 1 — CRM Foundation**
Auth (JWT + refresh), org creation, user invite, RBAC, multi-tenancy, Leads, Contacts, Companies, Pipelines, Deals, Tasks, Activities, Notes.

**Phase 2 — AI Agent**
AI Chat API, tool calling (8-10 tools), destructive-action confirmation flow, Customer 360 + AI summary, audit log.

**Phase 3 — RAG**
Document upload (PDF/DOCX/TXT) → chunk → embed → pgvector → tenant-scoped retrieval → chat context.

**Explicitly deferred (v2/later)** — Redis, rate limiting, lead scoring, scheduled deal-risk jobs, cost tracking dashboard, observability, real email sending, Kafka, integrations, Temporal, formal eval harness, Azure OpenAI live implementation.

## 6. Tech Stack

- Java 21, Spring Boot, Spring Security, Spring Data JPA, Spring Validation, Spring AI
- OpenAI (primary, live) + `LLMProvider` abstraction with Azure OpenAI as a stub for later
- PostgreSQL 16 (local install, not containerized) + pgvector extension, Flyway migrations
- Docker: optional, deferred — no cloud deployment either way
- JUnit 5 + Testcontainers (see open question #4 — confirm test depth before Step relies on it)

## 7. Architecture Rules (non-negotiable, enforce in code review at every step)

1. **Tenant isolation is code-enforced, not per-query discipline.** Use a base repository / Hibernate filter that injects `organization_id` automatically. (Resolves open question #3 — decided: enforce at framework level.)
2. **AI tools never touch repositories directly.** Flow is always: `AI → Tool → Application Service → Authorization → Repository`.
3. **CRM-retrieved content (notes, docs, emails) is treated as data, never as instructions** in the system prompt. Must be covered by an explicit injection test case.
4. **Destructive AI actions require explicit two-step confirmation** (preview + count → second confirm message), tracked via conversation state, not inferred from a bare "yes."
5. **RAG retrieval always filters by `organization_id` before similarity search** — never rely on vector similarity alone for tenant isolation.
6. **Every AI or user action that mutates data writes an `audit_logs` row** (source: MANUAL / AI / API / SYSTEM).

## 7a. Coding Conventions — Mirrored From Reference Project (`ecommerce-api`)

You asked for the same architecture/error-handling/logging/response style as your reference e-commerce API. That project has a consistent, recognizable pattern. It's mirrored here as the **default convention for all CRUD modules** (Stage 1–4 of plans.md). Two deliberate deviations are called out below and logged in §11 — read those before Stage 1.

**Package layout per module** (matches reference exactly):
```
com.<org>.salescrm.<module>
├── controller     (REST endpoints, thin — no business logic)
├── service         (interface)
├── service.impl    (implementation — all business logic + try/catch lives here)
├── repository      (Spring Data JPA)
├── entity
└── dto             (request/response shapes specific to this module)
```
Shared/cross-cutting code lives in a `common` module: `BaseEntity`, `Constants`, `LogConstants`, `ApiStatus` (the response wrapper), a `Resources`-style static helper class, and the JWT/security package — same as reference's `resources/`, `entities/BaseEntity.java`, and `security/` packages.

**`BaseEntity`** — every entity extends this (identical to reference):
```java
@MappedSuperclass
public class BaseEntity implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;
    public boolean isNew() { return id == null; }
    // getters/setters
}
```
CRM tenant-scoped entities additionally carry `organizationId` (per rule #1), which the reference project's single-tenant model didn't need.

**Response wrapper — `ApiStatus`.** Every service method returns one object (never throws to the controller), same as `EcomStatus`: a `statusType` (`SUCCESS`/`FAILURE`/`ERROR`/`UNAUTHORIZED`/`CREATED`), a `text` message, and one typed field per entity/list the response might carry (`lead`, `leads`, `deal`, `deals`, `total`, etc.). Controllers call a `Resources.formatedResponse(status, properties)` helper (Jackson `@JsonFilter` + `MappingJacksonValue`) to select which fields actually serialize per endpoint — identical mechanism to the reference's `formatedResponse`/`searchResponse`.

**`Constants`** — status-type strings and `|ENTITY|`-templated messages, same token-replacement pattern as reference (`Resources.setStatus(type, text, entity)` replacing `|ENTITY|`): `LIST_SUCCESS`, `DETAIL_SUCCESS`, `SAVE_SUCCESS`, `UPDATE_SUCCESS`, `DELETE_SUCCESS`, `SAVE_FAILURE`, `EXECUTION_ERROR`, `PARAMETER_MISSING`, `INVALID_TOKEN`, etc.

**`LogConstants`** — entity-name and action-name string constants (`LEAD`, `DEAL`, `TASK`, `LIST`, `ADD`, `EDIT`, `DELETE`) used to build consistent audit-log entries and log lines, same as reference.

**Service method pattern** — every service method: log entry with `LOGGER.info("XService >> methodName called!")`, resolve the caller's identity/org (see next point), do the work in a try block, return an `ApiStatus` with the right status/message/payload, catch `Exception` broadly and return `Constants.STATUS_ERROR` with `Constants.EXECUTION_ERROR + e.getMessage()` — never let exceptions propagate to the controller. This matches the reference's `LogServiceImpl` pattern exactly.

**Auth resolution pattern** — reference resolves the caller per-call via a `UserTokenService.getUserInfo(authToken)` call inside each service method (in addition to the JWT security filter chain validating the token at the gate). CRM mirrors this: a `TenantContextService.getCurrentContext(authToken)` (or resolved from `SecurityContext` — see §11 decision #7, flagged) that returns `{ userId, organizationId, role }`, called at the top of every service method needing tenant/user context, so services stay consistent whether called from a REST controller or an AI tool.

**Logging style** — SLF4J `Logger` per class (`LoggerFactory.getLogger(X.class)`), `LOGGER.info` at method entry and before/after key branches, matching the reference's verbosity. Use parameterized logging (`LOGGER.info("Lead created >> {}", lead.getId())`) rather than string concatenation — the one intentional style improvement over the reference, since concatenated logging is a minor perf/readability smell, not an architectural choice worth copying.

**Pagination** — request DTOs per searchable entity (`LeadSearchRequest`, `DealSearchRequest`, etc.) carrying `orderBy`, `pageNumber` (1-indexed at the API boundary, converted to 0-indexed internally), `pageSize`, with a `Resources.getDefaultRequest(request)` static helper providing defaults — identical to reference's `EcomSearchRequest` pattern.

**AI/RAG module is the one deliberate structural exception** — chat, tool-execution, and RAG-retrieval responses do **not** get forced into the shared `ApiStatus` God-object. They get their own dedicated DTOs (`ChatResponse { message, toolCalls[] }`, etc.) because their shape is inherently different per-call (streaming-friendly, variable tool output) and stuffing them into one wrapper class would make `ApiStatus` unmanageably large as AI features grow. Everything else (leads, contacts, companies, deals, tasks, notes, activities, audit logs) follows the shared `ApiStatus` pattern above.

## 8. Data Model (MVP entities)

```
organizations, users, invitations
user_types, roles, user_roles (join), user_logs
leads, contacts, companies
pipelines, pipeline_stages, deals
activities, tasks, notes
documents, document_chunks (pgvector)
conversations, conversation_messages, tool_executions
audit_logs
```

**`user_types` / `roles` / `user_roles`** — mirrored from the reference project's dual role model (its `UserType` + `Role`/`ERole` pattern). Reference keeps two separate concepts on `User`: a single `UserType` (`SUPER_ADMIN`/`ADMIN`/`VENDOR`/`CUSTOMER`/`MODERATOR`) as the business-type category, and a many-to-many `Set<Role>` (`ROLE_USER`/`ROLE_MODERATOR`/`ROLE_ADMIN`) that becomes Spring Security's granted authorities. CRM mirrors this exactly, even though for MVP the two will usually carry the same information:
- `user_types` — enum-backed (`EType` equivalent: `ORG_ADMIN`, `SALES_MANAGER`, `SALES_REP`), one row per type, FK'd from `users.user_type_id` (many-to-one, like reference).
- `roles` — enum-backed (`ERole` equivalent: `ROLE_ORG_ADMIN`, `ROLE_SALES_MANAGER`, `ROLE_SALES_REP`), joined via `user_roles(user_id, role_id)` many-to-many, exactly like reference's `user_roles` join table. This is what `@PreAuthorize` checks against.
- On registration, mirror reference's pattern: look up `UserType` by enum name via `userTypeRepository`, look up default `Role` via `roleRepository.findByName(...)`, assign both — don't invent a single-field shortcut.

**`user_logs`** — mirrored from reference's `UserLog` entity: `user_id` (FK), `action`, `sub_action`, `created_on`, `updated_on`, `is_active`, `is_deleted`, plus transient `user_name`/`user_type` convenience getters computed from the linked `User`. Exposed via a dedicated `LogService`/`LogServiceImpl` (`logs`, `viewLog`, `addLog`, `editLog`, `deleteLog`, plus a `createLog(user, action, subAction, createdOn, updatedOn)` helper other services call directly) — same shape as reference's `LogService`. **This is distinct from `audit_logs`** (see §11 decision #10) — don't merge them.

**Soft-delete convention** — every entity beyond `BaseEntity` carries `is_active` and `is_deleted` booleans (plus `created_on`/`updated_on` timestamps), matching every entity in the reference project (`User`, `UserType`, `Role`, `UserLog`, etc. all have this). "Delete" operations set `is_deleted = true` rather than issuing a SQL `DELETE`. All list/search queries must filter `is_deleted = false` — see §11 decision #11, this is a real behavior change from a naive CRUD delete and needs to be applied consistently from Stage 1 onward, not retrofitted later.

Full field-level detail for the remaining CRM-specific entities (leads, deals, etc.) lives in the PRD conversation history — plans.md steps specify fields per-entity as they're built.

## 9. Success Criteria ("done" for this portfolio project)

- App boots against local Postgres 16 with one command/script (Docker no longer required for MVP — see §11 decision #6).
- The "Killer Demo Flow" (see §10) runs end-to-end without manual DB intervention.
- ≥90% correct tool selection on a ~15-query manual test set.
- 0 hallucinated CRM facts across 10 recorded demo interactions.
- One passing test proving org A cannot read org B's data via API, chat, and RAG.
- One passing test proving a planted prompt-injection note does not trigger unintended tool execution.

## 10. Demo Flow (record this at the end)

```
Login → Create Org → Import ~20 leads → Create Pipeline → Create Deals
→ Add Activities → Open AI Assistant
→ "Show my 5 highest-value deals with no activity in 7 days"
→ "Create follow-up tasks for all of them tomorrow morning"
→ "Which deal is most likely to close?"
→ "Draft an email for [Company] explaining next steps" (uses RAG + Customer 360)
```

## 11. Open Decisions Log

| # | Question | Status |
|---|---|---|
| 1 | LLMProvider: OpenAI live, Azure stub only | **Decided** — as above |
| 2 | RAG chunking strategy | **Deferred** — start fixed ~500 tokens w/ overlap, tune later |
| 3 | Tenant isolation enforcement mechanism | **Decided** — framework-level filter, not per-query |
| 4 | Test coverage depth for "done" | **Open** — confirm before Step 1 of plans.md if it should include Testcontainers setup |
| 5 | Confirmation-flow statefulness | **Decided** — backend-tracked pending-confirmation state, not re-prompt-only |
| 6 | Postgres: local install vs Docker | **Decided** — use existing local Postgres 16, skip Postgres container. pgvector extension must be manually installed locally. Docker (for app container) is now optional/deferred, not part of MVP critical path. |
| 7 | Exception handling: reference project's per-method try/catch (services swallow exceptions, return `ApiStatus` with `STATUS_ERROR`, no `@RestControllerAdvice`) vs. the global exception handler originally specified in FR-checklist §6/§39 of the earlier PRD conversation | **Open — needs your call.** These two approaches conflict. Recommendation: keep the reference's per-method try/catch as the primary pattern (matches "same architecture" ask), but add a *thin* `@RestControllerAdvice` only as a last-resort safety net for things no service method can catch (malformed JSON body, 404 route not found, security-filter-level auth failures) — not as the primary error path. Confirm before Stage 1, since it affects how every service method in Stage 2 onward is written. |
| 8 | Auth resolution: per-call `TenantContextService.getCurrentContext(authToken)` (mirrors reference's `UserTokenService.getUserInfo(authToken)`) vs. pulling straight from Spring Security's `SecurityContext`/`@AuthenticationPrincipal` | **Open — needs your call.** Reference re-resolves the token manually inside every service method, independent of the security filter chain. This is more consistent (same code path for REST calls and AI tool calls) but is duplicate work if the JWT filter already populates `SecurityContext`. Default assumption unless you say otherwise: build `TenantContextService` as a thin wrapper that reads from `SecurityContext` (cheap, no re-parsing the token) but exposes the same `getCurrentContext()` shape the reference pattern expects, so the calling code in every service method looks identical to the reference. |
| 9 | Single `ApiStatus` response wrapper — reference project's version had ~40+ typed fields as the app grew. CRM will do the same for CRUD entities (leads/deals/contacts/etc.) but AI/chat/RAG responses are excluded (see §7a). | **Decided** — as described in §7a. Flagging only so it's not a surprise later when `ApiStatus` gets large; that's expected and matches the reference's own growth pattern. |
| 10 | `user_logs` (per-user action log, mirrored from reference's `UserLog`) vs. `audit_logs` (system-wide mutation/AI-action trail already planned in §7 rule #6) — these overlap in spirit. | **Decided, default given** — keep both, distinct purposes. `user_logs` mirrors the reference exactly: lightweight per-user activity trail (sign-in, list/view/add/edit/delete on any entity, status changes), written via the same `logService.createLog(...)` call pattern reference uses throughout its service layer. `audit_logs` stays the richer, AI-aware trail (resource type/id, `source: MANUAL/AI/API/SYSTEM`, metadata) that rule #6 and the destructive-confirmation flow depend on — that one cannot be dropped, it's load-bearing for the AI-safety demo. If this dual-logging feels redundant once built, collapsing `user_logs` into `audit_logs` is a fine simplification to make later, but build both first since that's the explicit ask. |
| 11 | Soft-delete convention (`is_active`/`is_deleted` booleans on every entity, mirrored from reference) vs. earlier plan steps that assumed straightforward CRUD `DELETE` | **Decided** — adopt soft-delete everywhere, matching reference. Concretely changes earlier-planned behavior: `DELETE` endpoints become "set `is_deleted=true`", and every list/search query (already being built with pagination) must add an `is_deleted = false` filter — this needs to go into the base repository/query pattern from Stage 1 onward, not bolted on later once several entities already exist without it. |

---
*This file is the reference. `plans.md` is the execution checklist — small steps only, each independently completable and demoable.*
