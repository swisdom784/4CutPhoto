# 4CutPhoto Android App Design

## Summary

4CutPhoto is a local-first Android app for collecting four-cut photo booth memories from services such as Life4Cuts and Photoism. The app opens directly to a QR scanner, downloads photo and video assets from scanned QR links when possible, and stores them as dated photo sessions with person tags. Users can review saved sessions in a calendar view or a chronological gallery view.

The first version does not include login, cloud sync, or backup export/import. It will use a local storage structure that keeps future manual export/import practical.

## Product Principles

- Open directly to scanning because capturing a new QR is the primary action.
- Keep navigation lightweight with a floating expandable menu instead of bottom tabs.
- Treat each QR result as one photo session.
- Apply person tags at the session level during save, then allow editing later from saved views.
- Keep data local by default and avoid account requirements in the first version.
- Use a modern, minimal sand-tone visual style with enough contrast and secondary accent colors.

## Primary Navigation

The app launches into the Scan screen.

There is no bottom tab bar. Main navigation uses a circular floating action button at the bottom right. The button contains a three-line menu icon. When tapped, it expands upward to reveal navigation actions:

- Scan
- Calendar
- Gallery

The current destination is highlighted or disabled in the expanded menu.

The Scan screen includes a back button at the top left. If Scan was opened from another in-app screen, the button returns to that screen. If Scan is the app entry screen, the button exits or follows the Android back behavior.

## Screens

### Scan

The Scan screen shows the camera preview with minimal chrome:

- Top-left back button
- Bottom-right expandable navigation button
- QR detection overlay if useful

When a QR code is detected, the app temporarily pauses duplicate recognition and validates the scanned value. Non-URL QR codes show an unsupported QR message. URL QR codes enter the download flow.

### Download Flow

The download flow is hybrid:

1. Try automatic download resolution from the scanned URL.
2. If photo/video files are found, download them into temporary app storage.
3. Show a pre-save preview screen with thumbnails and the person tag mini panel.
4. If automatic download fails, open the URL in an in-app WebView.
5. In WebView fallback, allow the user to select downloadable photo/video links or trigger supported downloads.
6. Send selected files into the same pre-save preview screen.

When the user confirms the preview, the app creates one PhotoSession containing all downloaded media.

### Calendar

Calendar shows a simple monthly calendar. Dates with saved sessions display a small colored dot. Tapping a date shows the sessions from that date below the calendar.

Each listed session shows:

- Cover image or video thumbnail
- Capture date/time
- Person tags
- Media count or video indicator

Tapping a session opens session detail.

### Gallery

Gallery is a chronological scroll view. Content is grouped by year and date with small text headings. Sessions appear below the date headings with their media previews.

The top area includes person tag filtering. Searching or selecting a person tag filters the gallery to sessions that include that person.

Tapping a session opens session detail.

### Session Detail

Session detail shows:

- Date at the top
- Applied person tags
- Media grid or pager for images and videos
- Tag editing entry point

Users can edit the session's person tags from this screen. The edit flow uses the same tag mini panel as the pre-save preview.

## Person Tag UX

The first version supports person tags only.

The tag mini panel appears during:

- Pre-save preview after scanning
- Session detail tag editing
- Calendar session editing
- Gallery session editing

The panel shows a small set of recent or frequently used person tags. It also includes a compact search/input field.

Behavior:

- Typing filters existing person tags.
- Tapping an existing tag applies it to the current session.
- If no matching tag exists, pressing enter or confirm creates a new PersonTag and applies it.
- Created tags are added to the global tag list.
- Long-pressing a tag reveals a delete action.
- Deleting a tag shows a confirmation dialog.
- Confirmed deletion removes the tag globally and removes it from all sessions.

Tags are stored at the session level in the first version. The data model can later be extended to per-media tags if needed.

## Visual Direction

The UI uses a warm sand-tone foundation while avoiding a one-note beige palette.

Suggested palette roles:

- Background: warm off-white
- Surface: light sand
- Primary text: charcoal
- Secondary text: warm gray
- Accent: muted olive or clay
- Warning/error: restrained red-brown

The design should feel modern, minimal, quiet, and personal. Cards should be subtle with small radii. The scanner remains functional and uncluttered.

## Technical Architecture

The app will be built with Kotlin and Jetpack Compose in Android Studio.

Recommended architecture:

- Compose UI screens
- Screen-level ViewModels
- Repository layer for sessions, media, tags, and download flow
- Room for local metadata
- App internal storage for media files
- Kotlin Coroutines and Flow for asynchronous work

Core libraries:

- CameraX for camera preview
- ML Kit Barcode Scanning for QR recognition
- WebView for fallback download pages
- Room for persistence
- Coil for image loading and thumbnails
- Navigation Compose or a small app-level state navigator

## Download Components

### QrScanService

Receives QR scan results, filters duplicate scans, validates URLs, and starts the download flow.

### DownloadResolver

Attempts automatic resolution of downloadable assets from a QR URL. It should detect direct image/video links and simple downloadable resources. If it cannot confidently resolve media, it returns a fallback result.

### DownloadWebView

Loads the original QR URL when automatic resolution fails. The WebView allows user-assisted media selection and passes selected downloads into the same save pipeline.

### MediaStoreService

Handles temporary downloads, final internal app storage, optional future export to the Android system gallery, and cleanup after failed saves.

## Data Model

### PhotoSession

- id
- capturedAt
- sourceQrUrl
- createdAt
- updatedAt
- coverMediaId

### MediaItem

- id
- sessionId
- type: image or video
- localPath
- mimeType
- fileName
- width
- height
- durationMillis
- createdAt

### PersonTag

- id
- name
- createdAt
- lastUsedAt

### SessionTagCrossRef

- sessionId
- tagId

## Storage Layout

Media files are stored in app internal storage by session.

```text
media/
  sessions/
    <session-id>/
      original/
        image_001.jpg
        video_001.mp4
      thumbnails/
        image_001.webp
```

The first version will not implement export/import, but this structure keeps future backup packaging straightforward. A later backup can combine Room metadata export with the media folder tree.

## Error Handling

- Non-URL QR: show an unsupported QR message.
- Duplicate scan: ignore repeated results while a flow is active.
- Automatic download failure: open WebView fallback.
- Network failure: show retry.
- Expired or blocked URL: allow reopening the page in WebView and explain that the source page may no longer provide downloadable media.
- Save failure: clean temporary files and allow retry.
- Tag deletion: require confirmation because it removes the tag from all sessions.

## Testing Strategy

Initial tests should cover:

- Room DAO behavior for sessions, media, tags, and cross references.
- Person tag creation, search, application, deletion, and global removal from sessions.
- Session repository save flow.
- DownloadResolver success and fallback behavior with controlled URL/content inputs.
- Calendar date grouping.
- Gallery tag filtering.

Manual QA should cover:

- Launching directly into Scan.
- Scanning a supported QR URL.
- Automatic download success path.
- WebView fallback path.
- Creating a new tag from the mini panel.
- Applying existing tags.
- Deleting a tag with confirmation.
- Viewing saved sessions in Calendar.
- Viewing and filtering saved sessions in Gallery.
- Editing tags after save.

## Explicit Non-Goals For Version 1

- Login
- Cloud sync
- Automatic cross-device migration
- Full export/import implementation
- Server-side scraping
- Social sharing features
- Per-media tags

