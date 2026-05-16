# Release Management MVP

## Business Requirements

A web-based Release Management MVP with three main sections: Releases, Environments, and Components.

### Releases
- Releases move through a lifecycle: **NEW -> PLANNED -> IN_PROGRESS -> FINISHED**
- A release can be **CANCELLED** before it is finished
- The UI shows releases grouped by status in columns (Kanban-style board)
- Users can create new releases
- Status transitions are enforced (e.g., NEW cannot jump to FINISHED)

### Environments
- Fixed list: DEV01, DEV02, SIT1, SIT2, UAT1, UAT2
- Environments are pre-seeded on application startup
- Each environment has a type: DEV, SIT, or UAT

### Components
- Components represent deployable artifacts (e.g., payment-service, frontend-app)
- Each component has a **name** and an **owner** (contact person/team)
- Components serve as templates for release artifacts

### Release Artifacts
- When a release moves to IN_PROGRESS, it must be assigned:
  - One SIT environment
  - One UAT environment
  - A list of artifacts (each artifact is a component + version + optional pipeline URL + owner)
- Artifact versions are per-release (the same component can have different versions in different releases)
- Artifacts can be edited while a release is in NEW, PLANNED, or IN_PROGRESS status
- When adding artifacts to a release, all components are pre-selected by default; the user specifies version for each

### UI/UX Priority
- Slick, professional, gorgeous UI with very simple features
- Brand colors: Accent Yellow `#ecad0a`, Blue Primary `#209dd7`, Purple Secondary `#753991`, Dark Navy `#032147`, Gray Text `#888888`
- No user management for the MVP

---

## Technical Architecture

### Frontend
- **Next.js 16** with Turbopack, client-rendered (`'use client'`)
- **TypeScript** with strict checking
- **Tailwind CSS v4** for styling
- **@base-ui/react** for accessible UI primitives (Select, Dialog, etc.)
- **lucide-react** for icons
- Location: `frontend/` subdirectory

### Backend
- **Spring Boot 3.4.4** with Java 21
- **JPA/Hibernate** with SQLite database
- **Lombok** for boilerplate reduction
- REST API with CORS configured for `localhost:3000`
- Location: `backend/` subdirectory

### Database
- **SQLite** with Hibernate 6.6 dialects
- Auto-DDL enabled (`spring.jpa.hibernate.ddl-auto`)
- File-based: `releasemgmt.db` in the backend directory

### Key Entities

| Entity | Description |
|--------|-------------|
| `Release` | Name, status (enum), SIT/UAT environment references, list of artifacts |
| `Environment` | Name, type (DEV/SIT/UAT) |
| `Component` | Name, owner, optional default pipeline URL |
| `ReleaseArtifact` | Join entity linking Release + Component, with per-release version, pipelineUrl, owner |

### REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/releases` | List all releases with artifacts and environments (JOIN FETCH) |
| GET | `/api/releases/{id}` | Get single release |
| POST | `/api/releases` | Create new release |
| PUT | `/api/releases/{id}/status` | Update release status |
| PUT | `/api/releases/{id}/assign` | Assign SIT/UAT environments + artifacts |
| PUT | `/api/releases/{id}/artifacts` | Update release artifacts |
| GET | `/api/environments` | List all environments |
| GET | `/api/components` | List all components |
| POST | `/api/components` | Create new component |

### Frontend Components

| Component | Purpose |
|-----------|---------|
| `ReleasesPage` | Kanban board with status columns, release cards, detail dialog |
| `AssignDialog` | Dialog for assigning environments + artifacts when moving to IN_PROGRESS |
| `EditArtifactsDialog` | Standalone dialog for editing a release's artifacts |
| `CreateReleaseDialog` | Dialog for creating a new release |
| `CreateComponentDialog` | Dialog for creating a new component |
| `ReleaseArtifactEditor` | Shared reusable component for artifact selection/version editing |
| `StatusBadge` | Visual badge for release status |

### Data Flow
1. Frontend fetches releases, environments, and components on page load
2. User interacts with the Kanban board (click cards, move status, edit artifacts)
3. All mutations go through the API layer (`src/lib/api.ts`)
4. After each mutation, `fetchData()` refreshes the local state

---

## Completed Features

- [x] Kanban board with 5 status columns
- [x] Create new release
- [x] Status transitions with validation
- [x] Environment assignment (SIT + UAT)
- [x] Artifact management (create, update per release)
- [x] Component catalog (create, list)
- [x] Environment list view
- [x] Release detail view with artifact listing
- [x] Data migration from old `artifacts` table to `components` table
- [x] Backend unit and integration tests (25 tests passing)
- [x] Type-safe frontend with TypeScript

## Known Limitations / Future Work

- No global exception handling (raw `IllegalArgumentException` / `IllegalStateException` thrown)
- No user authentication or authorization
- No audit trail or history
- No deployment tracking (releases are just metadata)
- SQLite is file-based; not suitable for concurrent multi-user access
- No pagination on list endpoints
- Frontend forms have minimal validation (backend validates, frontend shows alerts)
