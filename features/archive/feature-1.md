## Feature-1: Extend Environment Management

**Original Requirement:**
extend the environment management, so that we could add new environment or disable an existing environment.

**Date Completed:** 2026-04-18
**Approach:** Pragmatic Balance (Approach 3)

---

### What Was Built

#### Backend (Spring Boot)

1. **Environment entity** (`backend/src/main/java/com/releasemgmt/model/Environment.java`)
   - Added `active` boolean field with `@Builder.Default private boolean active = true`
   - Enables soft-delete pattern: disabled environments remain in DB with `active = false`

2. **DTOs** (`backend/src/main/java/com/releasemgmt/dto/`)
   - `EnvironmentDto`: added `active` field
   - `EnvironmentRequestDto` (new): simple request DTO with `name` and `type`

3. **EnvironmentService** (`backend/src/main/java/com/releasemgmt/service/EnvironmentService.java`)
   - `createEnvironment`: validates non-empty name/type, trims input, checks name uniqueness via `findByName`, catches `DataIntegrityViolationException` for race-condition safety
   - `toggleEnvironment`: flips `active` flag by ID
   - `getActiveEnvironments`: filters to active only (unused but available)

4. **EnvironmentController** (`backend/src/main/java/com/releasemgmt/controller/EnvironmentController.java`)
   - `POST /api/environments` - create new environment
   - `PUT /api/environments/{id}/toggle` - disable/enable environment

5. **ReleaseService** (`backend/src/main/java/com/releasemgmt/service/ReleaseService.java`)
   - `assignEnvironments`: validates that chosen SIT and UAT environments are `active`, throws `IllegalStateException` if disabled
   - `createRelease`: validates non-empty release name

6. **DataInitializer** (`backend/src/main/java/com/releasemgmt/config/DataInitializer.java`)
   - Emptied: no pre-seeded environments. All environments are now managed via UI.

#### Frontend (Next.js)

1. **Types** (`frontend/src/lib/types.ts`)
   - `Environment` interface now includes `active: boolean`

2. **API client** (`frontend/src/lib/api.ts`)
   - `createEnvironment(name, type)` - POST to `/api/environments`
   - `toggleEnvironment(id)` - PUT to `/api/environments/{id}/toggle`

3. **CreateEnvironmentDialog** (`frontend/src/components/CreateEnvironmentDialog.tsx`)
   - New dialog component following existing patterns
   - Name input + type select (DEV/SIT/UAT)
   - Form validation (non-empty name and type)

4. **Environments page** (`frontend/src/app/environments/page.tsx`)
   - Added "Create Environment" button in header
   - Each environment card shows Disable/Enable button
   - Disabled environments styled with gray border, opacity-60, and red "Disabled" badge
   - Lists active releases assigned to each environment
   - Empty state when no environments exist

5. **AssignDialog** (`frontend/src/components/AssignDialog.tsx`)
   - Filters out inactive environments from SIT/UAT dropdowns: `e.active !== false`

#### Tests

1. **ReleaseServiceTest** (`backend/src/test/java/com/releasemgmt/service/ReleaseServiceTest.java`)
   - Updated `Environment` constructor calls to include `active` parameter

2. **EnvironmentControllerIntegrationTest** (`backend/src/test/java/com/releasemgmt/controller/EnvironmentControllerIntegrationTest.java`)
   - Rewritten: tests empty initial state, create environment, toggle active state

3. **ReleaseControllerIntegrationTest** (`backend/src/test/java/com/releasemgmt/controller/ReleaseControllerIntegrationTest.java`)
   - `assignEnvironments_shouldSucceed` now creates its own SIT/UAT environments instead of relying on pre-seeded data

---

### Key Decisions

1. **Soft-delete via `active` flag**: Disabled environments are not deleted; they remain in the DB with `active = false`. Releases already assigned to a disabled environment keep their assignment.
2. **No pre-seeded data**: DataInitializer was emptied. Environments are fully managed through the UI.
3. **Backend enforces active validation**: `ReleaseService.assignEnvironments` rejects assignments to disabled environments, ensuring consistency even if frontend filtering is bypassed.
4. **Race-condition safety**: `createEnvironment` catches `DataIntegrityViolationException` from the DB unique constraint as a fallback to the `findByName` check.

---

### Files Modified

**Backend:**
- `backend/src/main/java/com/releasemgmt/model/Environment.java`
- `backend/src/main/java/com/releasemgmt/dto/EnvironmentDto.java`
- `backend/src/main/java/com/releasemgmt/dto/EnvironmentRequestDto.java` (new)
- `backend/src/main/java/com/releasemgmt/service/EnvironmentService.java`
- `backend/src/main/java/com/releasemgmt/controller/EnvironmentController.java`
- `backend/src/main/java/com/releasemgmt/service/ReleaseService.java`
- `backend/src/main/java/com/releasemgmt/config/DataInitializer.java`
- `backend/src/test/java/com/releasemgmt/service/ReleaseServiceTest.java`
- `backend/src/test/java/com/releasemgmt/controller/EnvironmentControllerIntegrationTest.java`
- `backend/src/test/java/com/releasemgmt/controller/ReleaseControllerIntegrationTest.java`

**Frontend:**
- `frontend/src/lib/types.ts`
- `frontend/src/lib/api.ts`
- `frontend/src/components/CreateEnvironmentDialog.tsx` (new)
- `frontend/src/app/environments/page.tsx`
- `frontend/src/components/AssignDialog.tsx`

---

### Test Results

All 27 tests pass:
- `EnvironmentControllerIntegrationTest`: 3 tests
- `ComponentControllerIntegrationTest`: 3 tests
- `ReleaseControllerIntegrationTest`: 6 tests
- `ComponentServiceTest`: 2 tests
- `ReleaseServiceTest`: 13 tests
