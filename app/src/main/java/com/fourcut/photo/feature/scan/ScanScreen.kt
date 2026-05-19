package com.fourcut.photo.feature.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.fourcut.photo.core.designsystem.component.QuietStateCard
import com.fourcut.photo.core.designsystem.component.QuietStateKind
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }
    var scanMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("‹")
        }

        if (hasCameraPermission) {
            CameraPreview(
                onScanResult = { result ->
                    when (result) {
                        is QrScanResult.AcceptedUrl -> {
                            scanMessage = null
                            onQrDetected(result.value)
                        }
                        is QrScanResult.Unsupported -> {
                            scanMessage = "This QR is not a download link."
                        }
                        QrScanResult.DuplicateIgnored,
                        QrScanResult.Ignored -> Unit
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            QuietStateCard(
                kind = QuietStateKind.CameraPermission,
                onPrimaryAction = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onSecondaryAction = { onQrDetected("https://example.com/photo.jpg") },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
        }

        scanMessage?.let { message ->
            ScanMessageCard(
                message = message,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun ScanMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    onScanResult: (QrScanResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var isProcessing by remember { mutableStateOf(false) }
    var lastAcceptedValue by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val scanner = BarcodeScanning.getClient()
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage == null) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                val rawValue = barcodes
                                    .firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                                    ?.rawValue
                                when (
                                    val result = classifyQrScan(
                                        rawValue = rawValue,
                                        lastAcceptedValue = lastAcceptedValue,
                                        isProcessing = isProcessing
                                    )
                                ) {
                                    is QrScanResult.AcceptedUrl -> {
                                        isProcessing = true
                                        lastAcceptedValue = result.value
                                        onScanResult(result)
                                    }
                                    is QrScanResult.Unsupported -> onScanResult(result)
                                    QrScanResult.DuplicateIgnored,
                                    QrScanResult.Ignored -> Unit
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    )
}
