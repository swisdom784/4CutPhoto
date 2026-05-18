# 4CutPhoto Android MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first local-first Android MVP for scanning photo booth QR links, saving downloaded photo/video sessions, tagging people, and browsing sessions in Calendar and Gallery views.

**Architecture:** Kotlin + Jetpack Compose app with a small MVVM structure. Room stores session metadata and person tags, app internal storage stores media files, and repositories expose Flow-based state to Compose screens.

**Tech Stack:** Android Studio, Kotlin, Jetpack Compose, CameraX, ML Kit Barcode Scanning, WebView, Room, Coil, Kotlin Coroutines/Flow, JUnit, Robolectric where needed.

---

## Scope Check

The design covers one coherent MVP with four connected flows: Scan, Download/Save, Calendar, and Gallery. Build this as one app plan, but keep each task independently testable and commit after every task.

## Target File Structure

Use a modern feature-first Android package structure. Shared platform code lives in `core`, persistence lives in `data`, app business contracts live in `domain`, and screen-specific Compose code lives under `feature`.

Create this Android project structure:

```text
settings.gradle.kts
build.gradle.kts
gradle.properties
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/com/fourcut/photo/MainActivity.kt
app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt
app/src/main/java/com/fourcut/photo/core/designsystem/theme/Color.kt
app/src/main/java/com/fourcut/photo/core/designsystem/theme/Theme.kt
app/src/main/java/com/fourcut/photo/core/designsystem/component/FloatingNavMenu.kt
app/src/main/java/com/fourcut/photo/core/designsystem/component/PersonTagMiniPanel.kt
app/src/main/java/com/fourcut/photo/core/download/DownloadResolver.kt
app/src/main/java/com/fourcut/photo/core/download/DownloadResult.kt
app/src/main/java/com/fourcut/photo/core/media/AppMediaStorage.kt
app/src/main/java/com/fourcut/photo/core/time/ClockProvider.kt
app/src/main/java/com/fourcut/photo/data/local/FourCutDatabase.kt
app/src/main/java/com/fourcut/photo/data/local/RoomConverters.kt
app/src/main/java/com/fourcut/photo/data/local/session/PhotoSessionEntity.kt
app/src/main/java/com/fourcut/photo/data/local/session/MediaItemEntity.kt
app/src/main/java/com/fourcut/photo/data/local/session/SessionTagCrossRef.kt
app/src/main/java/com/fourcut/photo/data/local/session/SessionDao.kt
app/src/main/java/com/fourcut/photo/data/local/tag/PersonTagEntity.kt
app/src/main/java/com/fourcut/photo/data/local/tag/PersonTagDao.kt
app/src/main/java/com/fourcut/photo/data/repository/SessionRepository.kt
app/src/main/java/com/fourcut/photo/data/repository/TagRepository.kt
app/src/main/java/com/fourcut/photo/domain/model/PhotoSession.kt
app/src/main/java/com/fourcut/photo/domain/model/SessionMedia.kt
app/src/main/java/com/fourcut/photo/domain/model/PersonTag.kt
app/src/main/java/com/fourcut/photo/domain/repository/SessionStore.kt
app/src/main/java/com/fourcut/photo/domain/repository/TagStore.kt
app/src/main/java/com/fourcut/photo/navigation/AppDestination.kt
app/src/main/java/com/fourcut/photo/feature/scan/QrScanState.kt
app/src/main/java/com/fourcut/photo/feature/scan/ScanScreen.kt
app/src/main/java/com/fourcut/photo/feature/download/DownloadFlowScreen.kt
app/src/main/java/com/fourcut/photo/feature/calendar/CalendarScreen.kt
app/src/main/java/com/fourcut/photo/feature/gallery/GalleryScreen.kt
app/src/main/java/com/fourcut/photo/feature/session/SessionDetailScreen.kt
app/src/test/java/com/fourcut/photo/data/local/SessionDaoTest.kt
app/src/test/java/com/fourcut/photo/data/local/PersonTagDaoTest.kt
app/src/test/java/com/fourcut/photo/data/repository/SessionRepositoryTest.kt
app/src/test/java/com/fourcut/photo/core/download/DownloadResolverTest.kt
```

Responsibilities:

- `core/download`: URL and content detection, not UI.
- `core/media`: app internal file layout and cleanup.
- `core/designsystem`: shared colors, theme, reusable UI components, and app-level visual language.
- `data/local`: Room schema and DAOs.
- `data/repository`: business operations that combine DAOs and storage.
- `domain/model`: UI-independent app models used across features.
- `domain/repository`: repository interfaces that feature code depends on.
- `feature/scan`, `feature/download`, `feature/calendar`, `feature/gallery`, `feature/session`: screen-level UI, state holders, and ViewModels when needed.
- `navigation`: app-level destinations and navigation state.

The plan's task descriptions use the same responsibilities. If Android Studio generates older `ui/*` paths during scaffolding, move screen code into the `feature/*` packages before committing the task.

## Task 1: Scaffold Android Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/fourcut/photo/MainActivity.kt`
- Create: `app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt`
- Create: `app/src/main/java/com/fourcut/photo/core/designsystem/theme/Color.kt`
- Create: `app/src/main/java/com/fourcut/photo/core/designsystem/theme/Theme.kt`

- [ ] **Step 1: Create or generate the Android project**

Use Android Studio's Empty Activity template if possible, with:

```text
Name: 4CutPhoto
Package name: com.fourcut.photo
Language: Kotlin
Minimum SDK: 26
Build configuration language: Kotlin DSL
UI: Jetpack Compose
```

If Android Studio generates newer dependency versions than the snippets below, keep Android Studio's generated versions and apply the same library set.

- [ ] **Step 2: Add dependency set**

In `app/build.gradle.kts`, ensure these capabilities exist:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.fourcut.photo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fourcut.photo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

- [ ] **Step 3: Add manifest permissions**

In `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:label="4CutPhoto"
        android:theme="@style/Theme.FourCutPhoto">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 4: Create app entry**

In `MainActivity.kt`:

```kotlin
package com.fourcut.photo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fourcut.photo.core.designsystem.theme.FourCutPhotoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FourCutPhotoTheme {
                FourCutPhotoApp()
            }
        }
    }
}
```

In `FourCutPhotoApp.kt`:

```kotlin
package com.fourcut.photo

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun FourCutPhotoApp() {
    Text("4CutPhoto")
}
```

- [ ] **Step 5: Add theme colors**

In `Color.kt`:

```kotlin
package com.fourcut.photo.core.designsystem.theme

import androidx.compose.ui.graphics.Color

val WarmWhite = Color(0xFFFFFCF7)
val SandBackground = Color(0xFFFAF6EE)
val SandSurface = Color(0xFFF0E7D8)
val PureWhite = Color(0xFFFFFFFF)
val Charcoal = Color(0xFF292724)
val WarmGray = Color(0xFF766F65)
val MutedOlive = Color(0xFF7C8062)
val Clay = Color(0xFFA66D55)
val Hairline = Color(0xFFE5DED2)
```

In `Theme.kt`:

```kotlin
package com.fourcut.photo.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    background = WarmWhite,
    surface = PureWhite,
    surfaceVariant = SandSurface,
    primary = MutedOlive,
    secondary = Clay,
    onBackground = Charcoal,
    onSurface = Charcoal
)

@Composable
fun FourCutPhotoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
```

- [ ] **Step 6: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties app
git commit -m "chore: scaffold Android Compose app"
```

## Task 2: Add Room Data Model

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/core/time/ClockProvider.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/local/RoomConverters.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/local/session/PhotoSessionEntity.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/local/session/MediaItemEntity.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/local/session/SessionTagCrossRef.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/local/tag/PersonTagEntity.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/local/session/SessionDao.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/local/tag/PersonTagDao.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/local/FourCutDatabase.kt`
- Test: `app/src/test/java/com/fourcut/photo/data/PersonTagDaoTest.kt`
- Test: `app/src/test/java/com/fourcut/photo/data/SessionDaoTest.kt`

- [ ] **Step 1: Write DAO tests first**

Create `PersonTagDaoTest.kt`:

```kotlin
package com.fourcut.photo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourcut.photo.data.local.FourCutDatabase
import com.fourcut.photo.data.local.tag.PersonTagEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PersonTagDaoTest {
    private lateinit var db: FourCutDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FourCutDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndSearchTagsByName() = runTest {
        db.personTagDao().insert(PersonTagEntity(name = "Hajin", createdAt = 1L, lastUsedAt = 1L))
        db.personTagDao().insert(PersonTagEntity(name = "JungHyun", createdAt = 2L, lastUsedAt = 2L))

        val result = db.personTagDao().searchByName("ha")

        assertEquals(1, result.size)
        assertEquals("Hajin", result.first().name)
    }

    @Test
    fun deleteTagRemovesItFromList() = runTest {
        val id = db.personTagDao().insert(PersonTagEntity(name = "Hajin", createdAt = 1L, lastUsedAt = 1L))

        db.personTagDao().deleteById(id)

        assertTrue(db.personTagDao().getAll().isEmpty())
    }
}
```

Create `SessionDaoTest.kt`:

```kotlin
package com.fourcut.photo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourcut.photo.data.local.FourCutDatabase
import com.fourcut.photo.data.local.session.MediaItemEntity
import com.fourcut.photo.data.local.session.PhotoSessionEntity
import com.fourcut.photo.data.local.session.SessionTagCrossRef
import com.fourcut.photo.data.local.session.MediaType
import com.fourcut.photo.data.local.tag.PersonTagEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SessionDaoTest {
    private lateinit var db: FourCutDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FourCutDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun sessionWithMediaAndTagsCanBeLoaded() = runTest {
        val sessionId = db.sessionDao().insertSession(
            PhotoSessionEntity(
                capturedAt = 10L,
                sourceQrUrl = "https://example.com/qr",
                sourceHost = "example.com",
                sourceLabel = "Example Booth",
                sessionIndexForDay = 1,
                createdAt = 10L,
                updatedAt = 10L
            )
        )
        db.sessionDao().insertMedia(
            MediaItemEntity(
                sessionId = sessionId,
                type = MediaType.IMAGE,
                localPath = "media/sessions/$sessionId/original/image_001.jpg",
                mimeType = "image/jpeg",
                fileName = "image_001.jpg",
                createdAt = 10L
            )
        )
        val tagId = db.personTagDao().insert(PersonTagEntity(name = "Hajin", createdAt = 10L, lastUsedAt = 10L))
        db.sessionDao().insertSessionTag(SessionTagCrossRef(sessionId = sessionId, tagId = tagId))

        val result = db.sessionDao().getSessionWithDetails(sessionId)

        assertEquals("https://example.com/qr", result.session.sourceQrUrl)
        assertEquals(1, result.media.size)
        assertEquals("Hajin", result.tags.first().name)
    }

    @Test
    fun sessionsFromSameDayRemainSeparate() = runTest {
        val firstSessionId = db.sessionDao().insertSession(
            PhotoSessionEntity(
                capturedAt = 10L,
                sourceQrUrl = "https://booth-a.example/qr-1",
                sourceHost = "booth-a.example",
                sourceLabel = "Booth A",
                sessionIndexForDay = 1,
                createdAt = 10L,
                updatedAt = 10L
            )
        )
        val secondSessionId = db.sessionDao().insertSession(
            PhotoSessionEntity(
                capturedAt = 20L,
                sourceQrUrl = "https://booth-b.example/qr-2",
                sourceHost = "booth-b.example",
                sourceLabel = "Booth B",
                sessionIndexForDay = 2,
                createdAt = 20L,
                updatedAt = 20L
            )
        )

        val sessions = db.sessionDao().getSessionsForDay(startMillis = 0L, endMillis = 86_400_000L)

        assertEquals(listOf(firstSessionId, secondSessionId), sessions.map { it.id })
        assertEquals(listOf(1, 2), sessions.map { it.sessionIndexForDay })
    }
}
```

- [ ] **Step 2: Run tests to verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*DaoTest"
```

Expected: fails because database/entities/DAOs do not exist.

- [ ] **Step 3: Add Room entities and DAOs**

Create entities and DAOs matching the tests. Use `Long` autogenerated ids and epoch millis for timestamps.

Key definitions:

```kotlin
enum class MediaType { IMAGE, VIDEO }
```

`PhotoSessionEntity` table: `photo_sessions` with `id`, `capturedAt`, `sourceQrUrl`, `sourceHost`, `sourceLabel`, `sessionIndexForDay`, `createdAt`, `updatedAt`, `coverMediaId`.

`MediaItemEntity` table: `media_items` with `id`, `sessionId`, `type`, `localPath`, `mimeType`, `fileName`, `width`, `height`, `durationMillis`, `createdAt`.

`PersonTagEntity` table: `person_tags` with `id`, unique indexed `name`, `createdAt`, `lastUsedAt`.

`SessionTagCrossRef` table: `session_tag_cross_ref` with composite primary key `sessionId`, `tagId`.

`SessionDao` must include `getSessionsForDay(startMillis: Long, endMillis: Long): List<PhotoSessionEntity>` ordered by `capturedAt ASC, id ASC`. This keeps multiple sessions from the same day separate and gives the repository a stable way to calculate `sessionIndexForDay`.

`SessionWithDetails` relation object should expose:

```kotlin
data class SessionWithDetails(
    @Embedded val session: PhotoSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val media: List<MediaItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SessionTagCrossRef::class,
            parentColumn = "sessionId",
            entityColumn = "tagId"
        )
    )
    val tags: List<PersonTagEntity>
)
```

- [ ] **Step 4: Run DAO tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*DaoTest"
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/data app/src/test/java/com/fourcut/photo/data
git commit -m "feat: add local Room data model"
```

## Task 3: Add Repositories for Tags and Sessions

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/data/repository/TagRepository.kt`
- Create: `app/src/main/java/com/fourcut/photo/data/repository/SessionRepository.kt`
- Test: `app/src/test/java/com/fourcut/photo/data/SessionRepositoryTest.kt`

- [ ] **Step 1: Write repository test**

Create `SessionRepositoryTest.kt`:

```kotlin
package com.fourcut.photo.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fourcut.photo.data.local.FourCutDatabase
import com.fourcut.photo.data.repository.SaveMediaInput
import com.fourcut.photo.data.repository.SessionRepository
import com.fourcut.photo.data.repository.TagRepository
import com.fourcut.photo.data.local.session.MediaType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SessionRepositoryTest {
    private lateinit var db: FourCutDatabase
    private lateinit var tagRepository: TagRepository
    private lateinit var sessionRepository: SessionRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FourCutDatabase::class.java).build()
        tagRepository = TagRepository(db.personTagDao())
        sessionRepository = SessionRepository(db.sessionDao(), tagRepository)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveSessionCreatesTagsAndAppliesThem() = runTest {
        val sessionId = sessionRepository.saveSession(
            capturedAt = 100L,
            sourceQrUrl = "https://example.com/qr",
            sourceHost = "example.com",
            sourceLabel = "Example Booth",
            media = listOf(
                SaveMediaInput(MediaType.IMAGE, "path/image.jpg", "image/jpeg", "image.jpg")
            ),
            tagNames = listOf("Hajin", "JungHyun")
        )

        val saved = db.sessionDao().getSessionWithDetails(sessionId)

        assertEquals(1, saved.media.size)
        assertEquals(listOf("Hajin", "JungHyun"), saved.tags.map { it.name }.sorted())
    }
}
```

- [ ] **Step 2: Run test to verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*SessionRepositoryTest"
```

Expected: fails because repositories do not exist.

- [ ] **Step 3: Implement repositories**

Create `TagRepository` with:

```kotlin
class TagRepository(private val dao: PersonTagDao) {
    suspend fun getOrCreateTag(name: String, nowMillis: Long = System.currentTimeMillis()): Long
    suspend fun search(query: String): List<PersonTagEntity>
    suspend fun deleteTag(tagId: Long)
}
```

Normalize tag names by trimming spaces. Reject blank names with `require`.

Create `SessionRepository` with:

```kotlin
data class SaveMediaInput(
    val type: MediaType,
    val localPath: String,
    val mimeType: String,
    val fileName: String,
    val width: Int? = null,
    val height: Int? = null,
    val durationMillis: Long? = null
)

class SessionRepository(
    private val sessionDao: SessionDao,
    private val tagRepository: TagRepository
) {
    suspend fun saveSession(
        capturedAt: Long,
        sourceQrUrl: String,
        sourceHost: String?,
        sourceLabel: String?,
        media: List<SaveMediaInput>,
        tagNames: List<String>
    ): Long
}
```

`saveSession` must insert the session, insert media, create/apply tags, and set `coverMediaId` to the first media item if present. It must also calculate `sessionIndexForDay` from existing sessions on the same local calendar date so the gallery can label multiple same-day sessions as separate cards.

- [ ] **Step 4: Run repository tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*SessionRepositoryTest"
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/data/repository app/src/test/java/com/fourcut/photo/data/SessionRepositoryTest.kt
git commit -m "feat: add session and tag repositories"
```

## Task 4: Add Media Storage Service

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/core/media/AppMediaStorage.kt`
- Test: `app/src/test/java/com/fourcut/photo/core/storage/AppMediaStorageTest.kt`

- [ ] **Step 1: Write storage test**

Create `AppMediaStorageTest.kt`:

```kotlin
package com.fourcut.photo.core.media

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class AppMediaStorageTest {
    @Test
    fun saveOriginalCreatesSessionScopedFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = AppMediaStorage(context)

        val result = storage.saveOriginal(
            sessionId = 42L,
            fileName = "image_001.jpg",
            input = ByteArrayInputStream("image bytes".toByteArray())
        )

        assertTrue(result.path.contains("media/sessions/42/original/image_001.jpg"))
        assertTrue(result.exists())
    }
}
```

- [ ] **Step 2: Run test to verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*AppMediaStorageTest"
```

Expected: fails because `AppMediaStorage` does not exist.

- [ ] **Step 3: Implement storage service**

Create `AppMediaStorage` with:

```kotlin
class AppMediaStorage(private val context: Context) {
    fun saveOriginal(sessionId: Long, fileName: String, input: InputStream): File
    fun sessionDirectory(sessionId: Long): File
    fun deleteSession(sessionId: Long)
}
```

Use:

```kotlin
File(context.filesDir, "media/sessions/$sessionId/original")
```

Sanitize file names to remove path separators before writing.

- [ ] **Step 4: Run storage test**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*AppMediaStorageTest"
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/core/media app/src/test/java/com/fourcut/photo/core/media
git commit -m "feat: add app media storage"
```

## Task 5: Build App Navigation and Floating Menu

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/navigation/AppDestination.kt`
- Create: `app/src/main/java/com/fourcut/photo/core/designsystem/component/FloatingNavMenu.kt`
- Modify: `app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt`

- [ ] **Step 1: Create destination enum**

```kotlin
enum class AppDestination {
    Scan,
    Calendar,
    Gallery
}
```

- [ ] **Step 2: Create floating menu**

`FloatingNavMenu` must render a bottom-right circular button with a menu icon. When expanded, show Scan, Calendar, Gallery above it. Use `IconButton`, `FloatingActionButton`, and Material icons if available.

Required API:

```kotlin
@Composable
fun FloatingNavMenu(
    current: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
)
```

- [ ] **Step 3: Update app root**

`FourCutPhotoApp` should default to `AppDestination.Scan`, show the current screen, and overlay `FloatingNavMenu` at bottom end.

Use temporary stub screen text for Calendar and Gallery until their dedicated screen tasks replace it.

- [ ] **Step 4: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds and app launches to the Scan stub screen.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt app/src/main/java/com/fourcut/photo/navigation app/src/main/java/com/fourcut/photo/core/designsystem/component
git commit -m "feat: add floating app navigation"
```

## Task 6: Build Person Tag Mini Panel

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/core/designsystem/component/PersonTagMiniPanel.kt`

- [ ] **Step 1: Add reusable panel**

Create a Compose component:

```kotlin
@Composable
fun PersonTagMiniPanel(
    selectedTags: List<String>,
    suggestedTags: List<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onCreateTag: (String) -> Unit,
    onDeleteTagRequested: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

Behavior:

- Show selected tags as chips.
- Show suggested tags matching the query.
- Use a single-line `TextField` as the mini search/input.
- IME Done calls `onCreateTag(query)` when query is not blank.
- Long press on a suggested tag calls `onDeleteTagRequested(tag)`.

- [ ] **Step 2: Add delete confirmation host**

Inside the panel, keep local state for the long-pressed tag and show an `AlertDialog`:

```kotlin
AlertDialog(
    onDismissRequest = { pendingDelete = null },
    confirmButton = {
        TextButton(onClick = {
            pendingDelete?.let(onDeleteTagRequested)
            pendingDelete = null
        }) { Text("Delete") }
    },
    dismissButton = {
        TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
    },
    title = { Text("Delete tag?") },
    text = { Text("This removes the tag from all saved sessions.") }
)
```

- [ ] **Step 3: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/core/designsystem/component/PersonTagMiniPanel.kt
git commit -m "feat: add person tag mini panel"
```

## Task 7: Implement Download Resolver Skeleton

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/core/download/DownloadResult.kt`
- Create: `app/src/main/java/com/fourcut/photo/core/download/DownloadResolver.kt`
- Test: `app/src/test/java/com/fourcut/photo/core/download/DownloadResolverTest.kt`

- [ ] **Step 1: Write tests**

Create tests for direct media URLs:

```kotlin
class DownloadResolverTest {
    @Test
    fun directImageUrlReturnsAutomaticMedia() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://example.com/photo.jpg")

        assertTrue(result is DownloadResult.Automatic)
        assertEquals("https://example.com/photo.jpg", (result as DownloadResult.Automatic).items.first().url)
    }

    @Test
    fun htmlPageReturnsWebViewFallback() = runTest {
        val resolver = DownloadResolver()

        val result = resolver.resolve("https://example.com/download")

        assertTrue(result is DownloadResult.NeedsWebView)
    }
}
```

- [ ] **Step 2: Run tests to verify failure**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*DownloadResolverTest"
```

Expected: fails because resolver does not exist.

- [ ] **Step 3: Implement resolver skeleton**

Create:

```kotlin
sealed interface DownloadResult {
    data class Automatic(val items: List<DownloadableMedia>) : DownloadResult
    data class NeedsWebView(val url: String) : DownloadResult
    data class Unsupported(val reason: String) : DownloadResult
}

data class DownloadableMedia(
    val url: String,
    val mimeType: String,
    val suggestedFileName: String
)
```

`DownloadResolver.resolve(url)` should:

- Return `Unsupported` for non-http URLs.
- Return `Automatic` for URLs ending in `.jpg`, `.jpeg`, `.png`, `.webp`, `.mp4`, `.mov`.
- Return `NeedsWebView` for other http/https URLs.

- [ ] **Step 4: Run resolver tests**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "*DownloadResolverTest"
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/core/download app/src/test/java/com/fourcut/photo/core/download
git commit -m "feat: add download resolver fallback model"
```

## Task 8: Implement Scan Screen Skeleton

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/feature/scan/QrScanState.kt`
- Create: `app/src/main/java/com/fourcut/photo/feature/scan/ScanScreen.kt`
- Modify: `app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt`

- [ ] **Step 1: Add scan state**

```kotlin
sealed interface QrScanState {
    data object Idle : QrScanState
    data class Detected(val value: String) : QrScanState
    data class Unsupported(val value: String) : QrScanState
}
```

- [ ] **Step 2: Build Compose scan screen**

Create `ScanScreen` with:

```kotlin
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

For this task, use a temporary camera stub surface with a "Scan QR" label and a debug button that calls `onQrDetected("https://example.com/photo.jpg")`. Task 9 replaces the stub with CameraX.

- [ ] **Step 3: Wire Scan as app default**

In `FourCutPhotoApp`, show `ScanScreen` for `AppDestination.Scan`. When QR is detected, store the pending URL state and show the download flow screen added in Task 10.

- [ ] **Step 4: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds and Scan is the first visible screen.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/feature/scan app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt
git commit -m "feat: add scan-first app shell"
```

## Task 9: Add CameraX and ML Kit QR Detection

**Files:**
- Modify: `app/src/main/java/com/fourcut/photo/feature/scan/ScanScreen.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Add runtime camera permission handling**

Use Compose permission request via `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`. Show a simple permission request surface if camera permission is missing.

- [ ] **Step 2: Add CameraX PreviewView**

Use `AndroidView` to host `PreviewView`, bind CameraX preview to lifecycle, and use back camera.

- [ ] **Step 3: Add ML Kit analyzer**

Attach `ImageAnalysis` analyzer that runs ML Kit Barcode Scanning. On QR value detected:

```kotlin
if (rawValue.startsWith("http://") || rawValue.startsWith("https://")) {
    onQrDetected(rawValue)
}
```

Keep a local `isProcessing` flag so the same QR is not emitted repeatedly while navigating.

- [ ] **Step 4: Manual test**

Run:

```bash
./gradlew :app:assembleDebug
```

Install from Android Studio and scan a test QR URL.

Expected: app detects the QR once and enters the download flow.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/feature/scan/ScanScreen.kt app/src/main/AndroidManifest.xml
git commit -m "feat: add QR scanner"
```

## Task 10: Build Download Flow UI

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/feature/download/DownloadFlowScreen.kt`
- Modify: `app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt`

- [ ] **Step 1: Create flow states**

Inside `DownloadFlowScreen.kt`, define:

```kotlin
sealed interface DownloadFlowUiState {
    data object Resolving : DownloadFlowUiState
    data class Preview(val sourceUrl: String, val items: List<PreviewMedia>) : DownloadFlowUiState
    data class NeedsWebView(val sourceUrl: String) : DownloadFlowUiState
    data class Error(val message: String) : DownloadFlowUiState
}

data class PreviewMedia(
    val localPath: String,
    val mimeType: String,
    val fileName: String
)
```

- [ ] **Step 2: Add preview screen**

Show thumbnails as simple boxes first, tag mini panel below, and Save/Cancel actions.

Required API:

```kotlin
@Composable
fun DownloadFlowScreen(
    sourceUrl: String,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
)
```

- [ ] **Step 3: Add WebView fallback**

For `NeedsWebView`, show an Android `WebView` with the source URL. Add a `DownloadListener` that captures the URL, content disposition, MIME type, and content length. Convert captured image/video downloads into `PreviewMedia` entries and show the same preview UI used by automatic downloads.

- [ ] **Step 4: Wire into app root**

When Scan emits a QR URL, show `DownloadFlowScreen`. On save/cancel, return to Scan.

- [ ] **Step 5: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/feature/download app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt
git commit -m "feat: add download flow screen"
```

## Task 11: Build Calendar Screen

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/feature/calendar/CalendarScreen.kt`
- Modify: `app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt`

- [ ] **Step 1: Create UI models**

```kotlin
data class CalendarDayUiModel(
    val dayOfMonth: Int,
    val hasSessions: Boolean,
    val isSelected: Boolean
)

data class CalendarSessionUiModel(
    val id: Long,
    val title: String,
    val timeLabel: String,
    val sourceLabel: String?,
    val sessionIndexForDay: Int,
    val tagNames: List<String>,
    val mediaCount: Int
)
```

- [ ] **Step 2: Create calendar composable**

Required API:

```kotlin
@Composable
fun CalendarScreen(
    days: List<CalendarDayUiModel>,
    sessionsForSelectedDay: List<CalendarSessionUiModel>,
    onDaySelected: (Int) -> Unit,
    onSessionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
)
```

Render a 7-column grid. Dates with sessions show a small clay or olive dot. When one date has several sessions, the selected-date list shows each session separately with a compact title such as "Session 1", its time, source label when available, tags, and media count.

- [ ] **Step 3: Wire temporary data**

In `FourCutPhotoApp`, show Calendar with sample data until repository wiring is added.

- [ ] **Step 4: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/feature/calendar app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt
git commit -m "feat: add calendar view"
```

## Task 12: Build Gallery and Session Detail Screens

**Files:**
- Create: `app/src/main/java/com/fourcut/photo/feature/gallery/GalleryScreen.kt`
- Create: `app/src/main/java/com/fourcut/photo/feature/session/SessionDetailScreen.kt`
- Modify: `app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt`

- [ ] **Step 1: Create gallery UI models**

```kotlin
data class GalleryDateGroupUiModel(
    val yearLabel: String,
    val dateLabel: String,
    val sessions: List<GallerySessionUiModel>
)

data class GallerySessionUiModel(
    val id: Long,
    val sessionTitle: String,
    val timeLabel: String,
    val sourceLabel: String?,
    val coverPath: String?,
    val tagNames: List<String>,
    val hasVideo: Boolean,
    val mediaSummary: String
)
```

- [ ] **Step 2: Build gallery screen**

Required API:

```kotlin
@Composable
fun GalleryScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    groups: List<GalleryDateGroupUiModel>,
    onSessionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
)
```

Show small year/date labels and distinct session cards below. Filtering by person tag will be connected after repository wiring. Same-day sessions must never merge media across QR scans; each card represents one `PhotoSession`.

For long same-day groups, use `LazyColumn` with stable session ids, compact metadata rows, consistent cover aspect ratios, and clear but restrained vertical spacing.

- [ ] **Step 3: Build session detail screen**

Required API:

```kotlin
@Composable
fun SessionDetailScreen(
    dateLabel: String,
    tagNames: List<String>,
    mediaPaths: List<String>,
    onBack: () -> Unit,
    onEditTags: () -> Unit,
    modifier: Modifier = Modifier
)
```

Show date and tags at the top, then a media grid. Use boxes for missing temporary sample paths and Coil for real paths.

- [ ] **Step 4: Wire temporary data**

Connect Gallery navigation to Session Detail in `FourCutPhotoApp` with in-memory sample data.

- [ ] **Step 5: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/fourcut/photo/feature/gallery app/src/main/java/com/fourcut/photo/feature/session app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt
git commit -m "feat: add gallery and session detail views"
```

## Task 13: Wire Repositories Into UI

**Files:**
- Modify: `app/src/main/java/com/fourcut/photo/FourCutPhotoApp.kt`
- Modify: `app/src/main/java/com/fourcut/photo/feature/download/DownloadFlowScreen.kt`
- Modify: `app/src/main/java/com/fourcut/photo/feature/calendar/CalendarScreen.kt`
- Modify: `app/src/main/java/com/fourcut/photo/feature/gallery/GalleryScreen.kt`
- Modify: `app/src/main/java/com/fourcut/photo/feature/session/SessionDetailScreen.kt`

- [ ] **Step 1: Create database in app root**

Use `remember` in `FourCutPhotoApp` for MVP wiring:

```kotlin
val context = LocalContext.current
val database = remember {
    Room.databaseBuilder(context, FourCutDatabase::class.java, "fourcut.db").build()
}
val tagRepository = remember { TagRepository(database.personTagDao()) }
val sessionRepository = remember { SessionRepository(database.sessionDao(), tagRepository) }
```

This can be moved to dependency injection later.

- [ ] **Step 2: Connect save flow**

In `DownloadFlowScreen`, call `sessionRepository.saveSession(...)` when the user confirms. Use the selected tag names from `PersonTagMiniPanel`.

- [ ] **Step 3: Connect tag search**

Use `LaunchedEffect(query)` to call `tagRepository.search(query)` and display results in the mini panel.

- [ ] **Step 4: Connect Calendar and Gallery**

Expose DAO Flow methods if not already present:

```kotlin
@Transaction
@Query("SELECT * FROM photo_sessions ORDER BY capturedAt DESC")
fun observeSessionsWithDetails(): Flow<List<SessionWithDetails>>
```

Map sessions into Calendar and Gallery UI models.

- [ ] **Step 5: Build and test**

Run:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: tests and build pass.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/fourcut/photo
git commit -m "feat: wire local data into app UI"
```

## Task 14: Add Final Manual QA Checklist

**Files:**
- Create: `docs/qa/android-mvp-checklist.md`

- [ ] **Step 1: Create checklist**

```markdown
# 4CutPhoto Android MVP QA Checklist

- [ ] App launches directly into Scan.
- [ ] Floating menu expands upward from the bottom-right button.
- [ ] Floating menu navigates to Scan, Calendar, and Gallery.
- [ ] Camera permission request appears when needed.
- [ ] QR scanner detects an HTTP or HTTPS QR once.
- [ ] Non-URL QR shows unsupported state.
- [ ] Direct image or video URL enters preview flow.
- [ ] Non-direct URL enters WebView fallback.
- [ ] Person tag mini panel filters existing tags.
- [ ] Entering a new name creates and applies a person tag.
- [ ] Long-press delete shows confirmation.
- [ ] Confirmed tag deletion removes the tag from existing sessions.
- [ ] Saving a session makes it appear in Gallery.
- [ ] Two QR scans saved on the same day appear as two separate Gallery session cards.
- [ ] A photo/video pair from one QR is not merged with another photo/video pair from another QR.
- [ ] Long same-day Gallery groups remain readable and scroll smoothly.
- [ ] Saving a session marks the date in Calendar.
- [ ] Calendar selected-date list shows multiple same-day sessions separately.
- [ ] Session detail shows date and person tags.
- [ ] Session detail shows session order and source hint when available.
- [ ] Session detail allows tag editing.
- [ ] The app uses a white-forward sand visual style with clear hierarchy and comfortable touch targets.
- [ ] App remains usable after closing and reopening.
```

- [ ] **Step 2: Run final verification**

Run:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: all unit tests pass and debug APK builds.

- [ ] **Step 3: Commit**

```bash
git add docs/qa/android-mvp-checklist.md
git commit -m "docs: add Android MVP QA checklist"
```
