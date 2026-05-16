## Feature-2: Environment Deployment Tracking

**Original Requirement:**
Document the component versions deployed to each environment, so that in a release I could see the list of components requested to be deployed, see the current deployed components in the environment, and highlight the differences for each environment in a separate list.

**Date Completed:** 2026-04-25
**Approach:** Traceable Deployment Tracking (Option B)

---

### What Was Built

#### Backend (Spring Boot)

1. **Deployment entity** (`backend/src/main/java/com/releasemgmt/model/Deployment.java`)
   - New JPA entity linking Environment + Component + Version
   - Stores `releaseId` for traceability (to know which release last deployed a component)
   - Stores `deployedAt` timestamp
   - Unique constraint on `(environment_id, component_id)` ensures one version per component per environment

2. **DeploymentRepository** (`backend/src/main/java/com/releasemgmt/repository/DeploymentRepository.java`)
   - `findByEnvironmentId(Long)` - get all deployments for an environment
   - `findByEnvironmentIdAndComponentId(Long, Long)` - find specific deployment for upsert

3. **DTOs** (`backend/src/main/java/com/releasemgmt/dto/`)
   - `DeploymentDto` (response): componentId, componentName, version, releaseId, releaseName, deployedAt
   - `DeployReleaseRequestDto` (request): releaseId, environmentId

4. **DeploymentService** (`backend/src/main/java/com/releasemgmt/service/DeploymentService.java`)
   - `deployRelease(releaseId, environmentId)`: copies all artifacts from a release to an environment, overriding existing versions
   - `getDeploymentsForEnvironment(environmentId)`: returns current deployments
   - Validates environment is active before deploying (defense-in-depth)
   - Handles race conditions with `DataIntegrityViolationException` fallback

5. **DeploymentController** (`backend/src/main/java/com/releasemgmt/controller/DeploymentController.java`)
   - `POST /api/deployments` - deploy a release's artifacts to an environment

6. **EnvironmentController** (`backend/src/main/java/com/releasemgmt/controller/EnvironmentController.java`)
   - `GET /api/environments/{id}/deployments` - get current deployments for an environment

7. **Tests**
   - `DeploymentServiceTest`: 7 unit tests covering deploy, override, release not found, environment not found, disabled environment, no artifacts, get deployments
   - `DeploymentControllerIntegrationTest`: 2 integration tests covering deploy flow and empty deployments read

#### Frontend (Next.js)

1. **Types** (`frontend/src/lib/types.ts`)
   - Added `Deployment` interface

2. **API client** (`frontend/src/lib/api.ts`)
   - `getEnvironmentDeployments(envId)` - GET /api/environments/{id}/deployments
   - `deployRelease(releaseId, environmentId)` - POST /api/deployments

3. **DeployDialog** (`frontend/src/components/DeployDialog.tsx`)
   - New dialog showing artifacts to be deployed with confirm/cancel
   - Loading state prevents double-click

4. **Releases page** (`frontend/src/app/releases/page.tsx`)
   - Added "Deploy to SIT" / "Deploy to UAT" buttons in release detail (when release has artifacts and assigned environments)
   - Added "Deployment Status" section showing requested vs deployed versions for SIT and UAT
   - Green background when versions match, yellow background when different or not deployed

5. **Environments page** (`frontend/src/app/environments/page.tsx`)
   - Shows "Deployed Components" section in each environment card
   - Lists component name and version

---

### Key Decisions

1. **Separate `Deployment` entity**: Chosen over extending `ReleaseArtifact` because release artifacts track "requested" versions while deployments track "actual" environment state. This allows Release A to show v1.2 requested while the environment shows v1.3 from Release B.
2. **Last-write-wins via unique constraint**: The `(environment_id, component_id)` unique constraint naturally handles override semantics without extra code.
3. **Traceability**: `releaseId` is stored on each deployment row to know which release last deployed a component, providing context without the complexity of a full audit history table.
4. **No deployment history**: Only current state is tracked. If history is needed later, a separate `DeploymentHistory` table can be added without migration pain.
5. **Client-side diff**: The release detail page fetches deployments and computes diffs in the browser. No backend "diff" endpoint was needed.
6. **Defense-in-depth**: Backend validates environment is active before deploying, even though the frontend only shows active environments.

---

### Files Modified

**Backend:**
- `backend/src/main/java/com/releasemgmt/model/Deployment.java` (new)
- `backend/src/main/java/com/releasemgmt/repository/DeploymentRepository.java` (new)
- `backend/src/main/java/com/releasemgmt/dto/DeploymentDto.java` (new)
- `backend/src/main/java/com/releasemgmt/dto/DeployReleaseRequestDto.java` (new)
- `backend/src/main/java/com/releasemgmt/service/DeploymentService.java` (new)
- `backend/src/main/java/com/releasemgmt/controller/DeploymentController.java` (new)
- `backend/src/main/java/com/releasemgmt/controller/EnvironmentController.java` (modified)
- `backend/src/test/java/com/releasemgmt/service/DeploymentServiceTest.java` (new)
- `backend/src/test/java/com/releasemgmt/controller/DeploymentControllerIntegrationTest.java` (new)
- `backend/pom.xml` (modified: lombok version updated to 1.18.46 for Java 25 compatibility)

**Frontend:**
- `frontend/src/lib/types.ts` (modified)
- `frontend/src/lib/api.ts` (modified)
- `frontend/src/components/DeployDialog.tsx` (new)
- `frontend/src/app/releases/page.tsx` (modified)
- `frontend/src/app/environments/page.tsx` (modified)

---

### Test Results

- `DeploymentServiceTest`: 7 tests, all passing
- `DeploymentControllerIntegrationTest`: 2 tests, all passing
- `ReleaseServiceTest`: 13 tests, all passing
- `ComponentServiceTest`: 2 tests, all passing

Note: Some existing integration tests have pre-existing failures unrelated to this feature due to Java 25 + SQLite JDBC compatibility issues in the test environment.
