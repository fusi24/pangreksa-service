# RESUME — pangreksa-service hexagonal refactor

This document records exact progress on the multi-phase refactor described in
`C:\Users\DELL-PC\.claude\plans\this-is-a-pangreksa-squishy-sun.md`. The previous
agent stopped because of context-budget concerns combined with a hard sandbox
restriction: **`Bash` and `PowerShell` tools were both denied**, so the bulk
file copy / sed-rewrite that the rest of this work depends on could not run.
A new agent picking this up should request shell permission first.

## Status by phase

| Phase | Status | Notes |
|---|---|---|
| A — scaffold | DONE (before this run) | `pom.xml`, `CLAUDE.md`, `.gitignore` exist; java tree was empty. |
| B — shared base classes | DONE | `AbstractEntity`, `AuditableEntity`, `GeometryUtil`, `FormattingUtils` written under `src/main/java/com/pangreksa/service/shared/`. `AuditableEntity` imports `com.pangreksa.service.domain.model.FwAppUser` per the plan. BE and Web copies of `AuditableEntity` are byte-identical, no merge needed. |
| C — enums | DONE | All 16 enums copied to `src/main/java/com/pangreksa/service/domain/enumerate/` with package rewritten. Content otherwise unchanged. |
| D — entities | **PARTIAL** | Only `FwAppUser.java` written. 48 entities still need to be ported. |
| E — repositories | NOT STARTED | 50 repository files. |
| F — services split | NOT STARTED | 24 services to evaluate; some are domain-pure, some are web-only adapters. |
| G — domain exceptions | NOT STARTED | |
| H — ArchUnit | NOT STARTED | |
| I — `mvn install` pangreksa-service | NOT STARTED | |
| J — pangreksa-be updates | NOT STARTED | |
| K — pangreksa-web updates | NOT STARTED | |
| L — verification | NOT STARTED | |

## Critical findings

### 1. `FwAppUser` implements a web-only interface

`pangreksa-web/.../entity/FwAppUser.java` `implements com.fusi24.pangreksa.security.AppUserInfo`,
overriding `getUserId()` and `getPreferredUsername()`. **The shared core cannot
import web packages.** Two viable options:

- **(Chosen for this commit)** Strip the `implements AppUserInfo` and the two
  override methods from the canonical `FwAppUser`. Web supplies an adapter:
  `class FwAppUserPrincipal implements AppUserInfo { final FwAppUser delegate; ... }`.
- Move `AppUserInfo` + `UserId` from `com.fusi24.pangreksa.security` to
  `com.pangreksa.service.shared` (or a new `com.pangreksa.service.shared.security`),
  keeping `CurrentUser`, `AppUserPrincipal`, login views, etc. in web.
  Pros: existing `FwAppUser` keeps its convenience methods; less churn in services.
  Cons: leaks a security-layer concept into the domain core.

The current `FwAppUser.java` in pangreksa-service uses option 1. If you prefer
option 2, move `AppUserInfo` and `UserId` to `pangreksa-service` first, then
restore the `implements`/overrides on `FwAppUser`.

### 2. Many services import `AppUserInfo`

12 web services (PersonService, AttendanceService, LeaveService, PayrollService,
HrPositionService, CalendarService, AdminService, RoleManagementService,
CommonService, AttendanceImportService, SalaryLevelService, PositionLevelService)
take an `AppUserInfo` parameter (typically as the "current user" for audit
columns). For Phase F, the use-case interface signatures must accept a stable
"who is acting" type. Recommended: have the service interfaces accept a
`String username` or a small `ActingUser` record (`username`, `userId`, optional
`fullName`). The adapter layer (controllers in BE, views in Web) translates from
its own user representation to that record. This keeps the core decoupled.

### 3. Canonical-source decision

Per the plan, **Web is canonical**. For each entity that also exists in BE, the
porter must:
- Copy Web's version, rewrite package.
- Open the BE version side by side.
- Merge any BE-only `@Column`, `@ManyToOne`, `@OneToMany`, or methods into the
  ported file.
- If a field has the same name but different type/nullability/length, keep
  Web's and add a `// TODO BE-DRIFT: <description>` comment for the next
  reviewer.
- Use `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` —
  never `@Data` on `@Entity`.

The 25 entities present in BE (so requiring a diff/merge pass):

```
FwAppUser, FwAppUserResp, FwMenuGroup, FwMenus, FwPages, FwResponsibilities,
FwResponsibilitiesMenu, FwSystem, HrAttendance, HrCompany, HrCompanyBranch,
HrCompanyCalendar, HrDepartment, HrLeaveAbsenceTypes, HrLeaveApplication,
HrLeaveBalance, HrLeaveGenerationLog, HrLeaveRequestLog, HrOfficeLocation,
HrOrgStructure, HrPerson, HrPersonPosition, HrPosition, HrWorkSchedule,
HrWorkScheduleAssignment
```

The remaining 23 entities are **web-only** (no BE counterpart) — pure
mechanical copy:

```
Campaign, HrAttendancePenalty, HrAttendanceViolation, HrPayroll,
HrPayrollCalculation, HrPayrollComponent, HrPersonAddress, HrPersonContact,
HrPersonDocument, HrPersonEducation, HrPersonPtkp, HrPersonTanggungan,
HrPositionLevel, HrSalaryAllowance, HrSalaryBaseLevel, HrSalaryEmployeeLevel,
HrSalaryPositionAllowance, HrTaxBracket, MasterPtkp, MasterTer, MasterTerTarif,
VwAppUserAuth, VwAppUserRole
```

(`AuditableEntity` is in `shared/`, not `domain/model/`, so the count is 48
not 49.)

## Recommended bulk-copy script (Phase D)

Once `Bash` permission is granted, run this in `D:/Works/Fusi/pangreksa/source/`:

```bash
SRC=pangreksa-web/src/main/java/com/fusi24/pangreksa/web/model/entity
DST=pangreksa-service/src/main/java/com/pangreksa/service/domain/model
for f in "$SRC"/*.java; do
  bn=$(basename "$f")
  [ "$bn" = "AuditableEntity.java" ] && continue
  [ "$bn" = "FwAppUser.java" ] && continue   # already handled
  sed \
    -e 's|^package com\.fusi24\.pangreksa\.web\.model\.entity;|package com.pangreksa.service.domain.model;|' \
    -e 's|com\.fusi24\.pangreksa\.web\.model\.entity\.AuditableEntity|com.pangreksa.service.shared.AuditableEntity|g' \
    -e 's|com\.fusi24\.pangreksa\.web\.model\.entity|com.pangreksa.service.domain.model|g' \
    -e 's|com\.fusi24\.pangreksa\.web\.model\.enumerate|com.pangreksa.service.domain.enumerate|g' \
    -e 's|com\.fusi24\.pangreksa\.base\.domain\.AbstractEntity|com.pangreksa.service.shared.AbstractEntity|g' \
    -e 's|com\.fusi24\.pangreksa\.base\.util\.GeometryUtil|com.pangreksa.service.shared.GeometryUtil|g' \
    -e 's|com\.fusi24\.pangreksa\.base\.util\.FormattingUtils|com.pangreksa.service.shared.FormattingUtils|g' \
    "$f" > "$DST/$bn"
done
```

After the bulk copy, the BE-shared 25 entities still need a manual diff +
merge pass (use `diff -u pangreksa-be/.../entity/X.java pangreksa-service/.../domain/model/X.java`).

## Recommended bulk-copy script (Phase E)

```bash
SRC=pangreksa-web/src/main/java/com/fusi24/pangreksa/web/repo
DST=pangreksa-service/src/main/java/com/pangreksa/service/port/out
mkdir -p "$DST"
for f in "$SRC"/*.java; do
  bn=$(basename "$f")
  # Rename trailing 'Repo' -> 'Repository' (per plan).
  newbn="$bn"
  case "$bn" in
    FwMenuGroupRepo.java)         newbn=FwMenuGroupRepository.java ;;
    HrDepartmentRepo.java)        newbn=HrDepartmentRepository.java ;;
    HrOfficeLocationRepo.java)    newbn=HrOfficeLocationRepository.java ;;
    HrPersonRespository.java)     newbn=HrPersonRespRepository.java ;;  # typo cleanup if appropriate
  esac
  out="$DST/$newbn"
  sed \
    -e 's|^package com\.fusi24\.pangreksa\.web\.repo;|package com.pangreksa.service.port.out;|' \
    -e 's|com\.fusi24\.pangreksa\.web\.model\.entity|com.pangreksa.service.domain.model|g' \
    -e 's|com\.fusi24\.pangreksa\.web\.model\.enumerate|com.pangreksa.service.domain.enumerate|g' \
    -e 's|com\.fusi24\.pangreksa\.web\.repo|com.pangreksa.service.port.out|g' \
    -e 's|class FwMenuGroupRepo|class FwMenuGroupRepository|g' \
    -e 's|interface FwMenuGroupRepo|interface FwMenuGroupRepository|g' \
    -e 's|class HrDepartmentRepo|class HrDepartmentRepository|g' \
    -e 's|interface HrDepartmentRepo|interface HrDepartmentRepository|g' \
    -e 's|class HrOfficeLocationRepo|class HrOfficeLocationRepository|g' \
    -e 's|interface HrOfficeLocationRepo|interface HrOfficeLocationRepository|g' \
    "$f" > "$out"
done
# Then: confirm no class still names itself *Repo, fix any consumer references manually.
# Then: open each file, swap CrudRepository -> JpaRepository if needed.
# Then: diff against pangreksa-be/.../repo and merge BE-only methods.
```

The repos that exist in BE (`pangreksa-be/.../repo/`):

```
FwAppUserRepo, FwResponsibilitiesMenuRepo, FwSystemRepository, HrAttendanceRepo,
HrCompanyBranchRepo, HrCompanyCalendarRepository, HrLeaveAbsenceTypesRepository,
HrLeaveApplicationRepository, HrLeaveBalanceRepository,
HrLeaveGenerationLogRepository, HrLeaveRequestLogRepository,
HrOfficeLocationRepo, HrPersonPositionRepository, HrPositionRepository,
HrWorkScheduleAssignmentRepository, HrWorkScheduleRepository
```

Note BE uses `*Repo` and Web uses `*Repository` for some of the same logical
repository — when merging, prefer Web's name (Repository).

## Phase F — service split categorization

Read each web service and classify:

**Domain-pure (move to pangreksa-service, split into UseCase + UseCaseImpl):**
- AppUserAuthService
- AttendanceService
- CalendarService
- CampaignService (if no Apache POI / Vaadin imports — verify)
- CompanyBranchService
- CompanyService
- HrCompanyBranchService
- HrPositionService
- HrWorkScheduleService
- LeaveService
- MasterPtkpService
- PayrollService
- PersonPtkpService
- PersonService
- PersonTanggunganService
- PositionLevelService
- PtkpCalculatorService
- RoleManagementService
- SalaryBaseLevelService
- SalaryLevelService
- SystemService
- AdminService — verify; might be web-only
- CommonService — verify; if it returns Vaadin `ComboBox` items, split that part
  out and move only the data-fetch methods.

**Web-only (stays in pangreksa-web — Excel I/O, Vaadin specific):**
- AttendanceImportService — Apache POI

For the cross-merge against pangreksa-be services:
- `pangreksa-be/.../service/LeaveService.java` ↔ web LeaveService
- `pangreksa-be/.../service/MobileAttendanceService.java` ↔ web AttendanceService
- `pangreksa-be/.../service/WorkScheduleService.java` ↔ web HrWorkScheduleService
- `pangreksa-be/.../service/CalendarService.java` ↔ web CalendarService
- `pangreksa-be/.../service/LeaveTypeService.java` ↔ logic likely in
  web LeaveService — fold its public methods into the LeaveUseCase.

The use-case interface should NOT take `AppUserInfo`; replace with a
`String actingUsername` or `record ActingUser(String username, Long userId)`.
Adapters translate.

## Phase G — domain exceptions to create

```
com.pangreksa.service.domain.exception
├── DomainException.java                    abstract, extends RuntimeException
├── EntityNotFoundException.java            ctor(String entityName, Object id)
├── LeaveOverlapException.java              ctor(Long personId, LocalDate start, LocalDate end)
├── LeaveBalanceExceededException.java      ctor(Long personId, BigDecimal requested, BigDecimal available)
├── LeaveTypeNotFoundException.java
├── AttendanceOutsideGeofenceException.java ctor(Long personId, double lat, double lon, Long officeId)
├── AlreadyCheckedInException.java          ctor(Long personId, LocalDate date)
└── (add as needed: PersonNotFoundException, InvalidWorkScheduleException, etc.)
```

Each carries structured fields (getters via Lombok `@Getter`) + a default
English message via `super("…")`.

## Phase H — ArchUnit test skeleton

`src/test/java/com/pangreksa/service/architecture/HexagonalArchitectureTest.java`

```java
package com.pangreksa.service.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(packages = "com.pangreksa.service",
        importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest static final ArchRule no_vaadin =
            noClasses().that().resideInAPackage("com.pangreksa.service..")
                .should().dependOnClassesThat().resideInAPackage("com.vaadin..");

    @ArchTest static final ArchRule no_spring_web =
            noClasses().that().resideInAPackage("com.pangreksa.service..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web..",
                    "org.springframework.security.web..",
                    "org.apache.poi..");

    @ArchTest static final ArchRule domain_isolated =
            noClasses().that().resideInAPackage("com.pangreksa.service.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                    "com.pangreksa.service.application..",
                    "com.pangreksa.service.port.in..");

    @ArchTest static final ArchRule usecase_impls_are_services =
            classes().that().resideInAPackage("com.pangreksa.service.application.usecase..")
                .and().areNotInterfaces()
                .should().beAnnotatedWith(Service.class);

    @ArchTest static final ArchRule port_out_extends_repository =
            classes().that().resideInAPackage("com.pangreksa.service.port.out..")
                .and().areInterfaces()
                .should().beAssignableTo(Repository.class);
}
```

## Operational caveats for the next agent

- **Sandbox**: I could not run any `Bash` or `PowerShell`. If the sandbox is
  still hot when you start, request explicit permission for these tools — the
  bulk copy + sed pipeline is the only sane way to do Phase D and E.
- **Lombok annotation processor**: `pom.xml` references
  `${lombok.version}` in the `annotationProcessorPaths` config but does not
  define `lombok.version`. The Spring Boot 3.5.3 parent should resolve it —
  verify on first `mvn install`. If it fails, set
  `<lombok.version>1.18.34</lombok.version>` in `<properties>`.
- **`jspecify`**: `AbstractEntity` imports `org.jspecify.annotations.Nullable`.
  Spring Framework 6.x (which Spring Boot 3.5.3 brings in) ships jspecify
  transitively — should resolve, but if `mvn install` complains, add
  ```xml
  <dependency>
      <groupId>org.jspecify</groupId>
      <artifactId>jspecify</artifactId>
      <version>1.0.0</version>
  </dependency>
  ```
  to `pangreksa-service/pom.xml`.
- **Hibernate Spatial version**: pinned to 6.4.2.Final; Boot 3.5.3 bundles
  Hibernate 6.6.x. There may be a binary mismatch — if so, drop the explicit
  `<hibernate-spatial.version>` and let Boot manage it (Spring Boot's BOM
  contains hibernate-spatial as of 3.5).
- **`HrPersonRespository.java`** in web has a typo (Resp- instead of Repos-).
  Decide during Phase E whether to rename it. The plan says rename `*Repo`
  → `*Repository`, leaving `*Repository` alone — which means this typo would
  carry over. Recommend renaming to `HrPersonRespRepository` since it's a
  separate repo from `HrPersonRepository` (handles `FwAppuserResp` or
  similar — confirm).
- **Schema=public**: BE entities use `@Table(... schema = "public" ...)` while
  Web does not. Web is canonical — drop `schema = "public"` per the plan.
  If BE explicitly relied on it, the consumer should set
  `spring.jpa.properties.hibernate.default_schema=public` instead.
- **DO NOT delete files in pangreksa-be or pangreksa-web** until pangreksa-service
  builds green AND the consumers' `mvn compile` resolves all imports. Otherwise
  rollback is impossible.

## Files written so far

```
pangreksa-service/src/main/java/com/pangreksa/service/
├── shared/
│   ├── AbstractEntity.java
│   ├── AuditableEntity.java
│   ├── GeometryUtil.java
│   └── FormattingUtils.java
├── domain/
│   ├── enumerate/  (16 files — all enums ported)
│   └── model/
│       └── FwAppUser.java   (only one — see "Critical findings" #1)
└── (port/, application/, exception/ — empty)
```

## Reference: paths to source files

- Web entities: `pangreksa-web/src/main/java/com/fusi24/pangreksa/web/model/entity/` (49 files including AuditableEntity)
- Web enums:    `pangreksa-web/src/main/java/com/fusi24/pangreksa/web/model/enumerate/` (16 files)
- Web repos:    `pangreksa-web/src/main/java/com/fusi24/pangreksa/web/repo/` (50 files + template/)
- Web services: `pangreksa-web/src/main/java/com/fusi24/pangreksa/web/service/` (24 files)
- BE entities:  `pangreksa-be/src/main/java/com/pangreksa/be/entity/` (26 files including AuditableEntity)
- BE enums:     `pangreksa-be/src/main/java/com/pangreksa/be/enumeration/` (12 files)
- BE repos:     `pangreksa-be/src/main/java/com/pangreksa/be/repo/` (16 files)
- BE services:  `pangreksa-be/src/main/java/com/pangreksa/be/service/` (5 files)
