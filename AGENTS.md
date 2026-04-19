# Release Management Project

## Business Requirements Management
- New feature would be put inside @features/workspace folder.
- During the summary phase, business requirement and implementation status should be archived to @features/archive folder and clean up workspace for new feature.

## Technical Details

- Implemented as a modern NextJS app, client rendered
- The NextJS app should be created in a subdirectory `frontend`
- Use spring boot as the backend and create that in a subdirectory `backend`
- Use sqlite as the db for now
- No user management for the MVP
- Use popular libraries
- As simple as possible but with an elegant UI
- prepare start and stop scripts for both `frontend` and `backend`

## Color Scheme

- Accent Yellow: `#ecad0a` - accent lines, highlights
- Blue Primary: `#209dd7` - links, key sections
- Purple Secondary: `#753991` - submit buttons, important actions
- Dark Navy: `#032147` - main headings
- Gray Text: `#888888` - supporting text, labels

## Strategy

1. Read the requirements from @features/workspace folder, turn it into design tasks and implemenation plan with success criteria for each phase to be checked off. 
2. Execute the plan ensuring all critiera are met
3. Carry out extensive integration testing with Playwright or similar, fixing defects
4. Only complete when the MVP is finished and tested, with the server running and ready for the user

## Coding standards

1. Use latest versions of libraries and idiomatic approaches as of today
2. Keep it simple - NEVER over-engineer, ALWAYS simplify, NO unnecessary defensive programming. No extra features - focus on simplicity.
3. Be concise. Keep README minimal. IMPORTANT: no emojis ever