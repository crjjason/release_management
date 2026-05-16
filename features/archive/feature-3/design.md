# Feature 3: Enlarge Release Details Popup — Architecture Design

## Explorer Findings

### Current Popup Location
- **File:** `frontend/src/app/releases/page.tsx`
- **Lines:** 324–432 (inline `<Dialog>` component)
- **Original size class:** `max-w-lg` on `<DialogContent>` (line 325)

### Dialog Component Structure
- **File:** `frontend/src/components/ui/dialog.tsx`
- The `DialogContent` component (lines 42–81) uses `@base-ui/react/dialog`.
- Default styling includes `sm:max-w-sm` (384px) hardcoded in the base component.
- The `className` prop is merged via `cn()` which uses `tailwind-merge`.

### Root Cause of "Too Small" Popup
`tailwind-merge` does **not** treat `sm:max-w-sm` and `max-w-lg` as conflicting classes because they have different prefixes. Therefore, when both are present:
- Below `sm` breakpoint: `max-w-lg` applies
- At `sm` and above: the base `sm:max-w-sm` (384px) **overrides** the intended wider width

This meant the popup was effectively locked at ~384px on desktop regardless of the `max-w-lg` override.

### Current Popup Content
The popup displays:
1. Release name (DialogTitle)
2. Status badge
3. SIT / UAT environment names
4. Requested artifacts list
5. Deployment status diff sections (SIT + UAT)
6. Action buttons: Deploy to SIT, Deploy to UAT, Edit Artifacts, status transitions

---

## Architecture Decision

**Approach:** Override the responsive breakpoint class directly with `sm:max-w-5xl` so it properly replaces the base `sm:max-w-sm` on desktop.

**Rationale:**
- Fixes the actual root cause: the base component's `sm:max-w-sm` was winning on desktop.
- `sm:max-w-5xl` (~1024px) provides ample space for all release details.
- Keeps existing component structure; no new files needed.
- Mobile behavior remains safe: the base `max-w-[calc(100%-2rem)]` still caps width on narrow screens.

**Trade-offs:**
- Very wide modals can feel disconnected on ultra-wide screens, but `max-w-5xl` is still centered and readable.
- No explicit `max-h` / `overflow-y-auto` was added. If a release has an extremely large number of artifacts, the modal could exceed viewport height. This is acceptable for the MVP; can be addressed later if needed.

---

## Component Design

### 1. `frontend/src/app/releases/page.tsx` — Release Detail Dialog

**Change:** Update the `<DialogContent>` invocation (line 325).

**Original:**
```tsx
<DialogContent className="max-w-lg">
```

**New:**
```tsx
<DialogContent className="sm:max-w-5xl">
```

**Responsibilities:**
- `sm:max-w-5xl` (64 rem / ~1024 px) replaces the base `sm:max-w-sm` on desktop, giving significantly more horizontal space.
- Mobile: unaffected because the base `max-w-[calc(100%-2rem)]` is still present.

---

## Implementation Map

| File | Action | Details |
|------|--------|---------|
| `frontend/src/app/releases/page.tsx` | Modify | Update `DialogContent` className (line 325) from `max-w-lg` to `sm:max-w-5xl` |

No new files. No backend changes.

---

## Data Flow

No data-flow changes. The popup is still read-only (except for button actions that open other dialogs). The only change is presentation.

---

## Build Sequence

1. **Update popup dimensions**
   - [x] Change `DialogContent` className to `sm:max-w-5xl`
2. **Visual verification**
   - [ ] Open a release with many artifacts and both SIT/UAT environments
   - [ ] Confirm popup is wider on desktop (~1024px)
   - [ ] Confirm mobile view still uses `max-w-[calc(100%-2rem)]` and is usable

---

## Critical Details

- **Responsive design:** The underlying `DialogContent` has `max-w-[calc(100%-2rem)]` for small viewports, so mobile remains safe. The `sm:max-w-5xl` class only activates at the `sm` breakpoint and above.
- **Testing:** Manual UI verification is sufficient; no unit tests required for pure styling changes.
- **Performance:** Negligible impact.
- **Accessibility:** No changes to focus management; `@base-ui/react-dialog` continues to handle the focus trap.
- **Follow-up consideration:** If releases with very long artifact lists cause the modal to exceed viewport height, consider adding `max-h-[90vh] overflow-y-auto` to `DialogContent` in a future iteration.
