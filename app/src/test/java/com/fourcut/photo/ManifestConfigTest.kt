package com.fourcut.photo

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ManifestConfigTest {
    @Test
    fun applicationUsesCustomLauncherIcon() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("android:icon=\"@mipmap/ic_launcher\""))
        assertTrue(manifest.contains("android:roundIcon=\"@mipmap/ic_launcher_round\""))
    }
}
