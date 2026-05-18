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
