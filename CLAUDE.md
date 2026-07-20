# CLAUDE.md

Guidance for working in this repository.

## Project

Spring PetClinic REST, converted from Spring `@RestController` classes to
**OfficeFloor function injection** (see commit "Conversion to OfficeFloor YAML
end point function injection"). Spring's DI, security, persistence and DTOs are
unchanged; only the request handling is now explicit function orchestration.

- OfficeFloor version: `4.0.2`, starter `net.officefloor.springboot:officefloor-rest-spring-boot-4-starter`.
- Docs: https://officefloor.net — tutorials at https://officefloor.net/tutorials/index.html
  (Spring REST series: `SpringRestFunctionHttpServer`, `SpringRestOrchestrationReference`,
  `SpringRestGovernanceHttpServer`, `SpringRestExceptionHttpServer`, `SpringRestVariableHttpServer`,
  `SpringRestConversionReference`). Fetch these when the primer below is insufficient — OfficeFloor
  is niche, so read the source/tutorials rather than assuming.

## OfficeFloor primer (how this codebase works)

A REST request is a **pipeline of small single-responsibility functions**, wired
declaratively in YAML. No controllers, no central route table.

### Endpoint YAML — `src/main/resources/officefloor/rest/**`
The **filename encodes the route**: `api/owners/{ownerId}.GET.yml` → `GET /api/owners/{ownerId}`.
Each file declares named steps:
```yaml
composition:
  authorize: "hasRole('OWNER_ADMIN')"   # Spring Security SpEL, applies to whole file
load:
  class: ...function.owner.LoadOwner
  govern: [ readonly-transaction ]      # governance wrapping this step
  next: respond                         # unconditional next step
respond:
  class: ...function.owner.RespondWithOwner
  govern: [ readonly-transaction ]
```
Keys: `class` (function impl), `next` (next step), `outputs` (named conditional
branches → steps), `govern` (list of governances), `composition.authorize`
(file-wide security). A single-step endpoint just names one `class`.

### Functions — `src/main/java/.../rest/function/**`
Each has a `service(...)` method. Parameters are resolved by role:
- `@PathVariable(name="ownerId") Integer id` — from the URL
- `@RequestBody Xxx dto` — HTTP request body (only ONE function per pipeline may bind it)
- Spring beans (`OwnerRepository`, `OwnerMapper`, …) — injected normally
- `Out<T> loaded` (`net.officefloor.plugin.variable.Out`) — **publishes** a value downstream
- `@Val T value` (`net.officefloor.plugin.variable.Val`) — **consumes** a published value
- `ObjectResponse<Dto> response` (`net.officefloor.web.ObjectResponse`) — sends the response body

So data flows between steps via `Out<T>` → `@Val T`, not return values. DTOs
appear only at the edges (request-body entry, response exit); models flow in between.

### Naming conventions (function classes)
`Load<E>` (fetch by path var) · `Build<E>` (construct from body) · `Validate<E>` ·
`Apply<E>` (mutate) · `Save<E>` (persist) · `Delete<E>` · `List<E>` ·
`RespondWith<E>` (200) · `RespondWith<E>Created` (201) · `RespondWithNoContent` (204).

### Governance — `govern: [ transaction | readonly-transaction ]`
Wraps a function's lifecycle (begin / commit-on-success / rollback-on-escalation).
`transaction` and `readonly-transaction` are provided by the Spring integration
(no `officefloor/govern/` dir in this project). Custom governances would live in
`src/main/resources/officefloor/govern/`.

### Escalations (exceptions) — `src/main/resources/officefloor/escalation/**`
Functions throw checked exceptions; handlers are wired by filename = fully
qualified exception class + `.yml`:
```yaml
handle:
  class: ...escalation.NotFoundExceptionHandler
```
Global handlers here cover `NotFoundException`, `DataIntegrityViolationException`,
`MethodArgumentNotValidException`, `AuthorizationDeniedException`, and a catch-all
`java.lang.Exception`. Precedence: method (on a step) > composition (file-wide) >
global (these files) > fall through to Spring `@ControllerAdvice`.
