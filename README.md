# Release Management

A web application for managing software releases, environments, and deployment artifacts.

## Features

- **Releases**: Create releases and move them through a lifecycle (New -> Planned -> In Progress -> Finished). Releases can be cancelled at any time before finishing.
- **Environments**: Predefined environments (DEV01, DEV02, SIT1, SIT2, UAT1, UAT2) with active release visibility.
- **Artifacts**: Manage deployable components with name, version, owner, and optional pipeline URL.
- **Assignment**: When a release moves to In Progress, assign SIT and UAT environments along with a list of artifacts to deploy.

## Tech Stack

- **Frontend**: Next.js 16 (client rendered), React 19, Tailwind CSS 4, Base UI, TypeScript
- **Backend**: Spring Boot 3.4, Java 21
- **Database**: SQLite
- **Tests**: Playwright (E2E), JUnit 5 + Mockito (unit), Spring Boot Test (integration)

## Project Structure

```
.
├── frontend/          # Next.js app
├── backend/           # Spring Boot app
├── start-all.sh       # Start both services
├── stop-all.sh        # Stop both services
├── start-backend.sh   # Start backend only
├── stop-backend.sh    # Stop backend only
├── start-frontend.sh  # Start frontend only
├── stop-frontend.sh   # Stop frontend only
```

## Running the App

```bash
./start-all.sh
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

To stop:

```bash
./stop-all.sh
```

## Running Tests

**Backend (unit + integration):**

```bash
cd backend && mvn test
```

**Frontend E2E:**

```bash
cd frontend && npx playwright test
```

## Color Scheme

- Accent Yellow: `#ecad0a`
- Blue Primary: `#209dd7`
- Purple Secondary: `#753991`
- Dark Navy: `#032147`
- Gray Text: `#888888`
