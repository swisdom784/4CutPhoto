package com.fourcut.photo.feature.scan

import android.Manifest
import android.content.pm.PackageManager
import android.view.MotionEvent
import androidx.camera.core.Camera
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.FocusMeteringAction
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Composable
fun ScanScreen(
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
        if (hasCameraPermission) {
            CameraPreview(
                onScanResult = { result ->
                    when (result) {
                        is QrScanResult.AcceptedUrl -> {
                            scanMessage = null
                            onQrDetected(result.value)
                        }
                        is QrScanResult.Unsupported -> {
                            scanMessage = "다운로드 링크가 아닌 QR이에요."
                        }
                        QrScanResult.DuplicateIgnored,
                        QrScanResult.Ignored -> Unit
                    }
                },
                focusPolicy = remember { ScanFocusPolicy() },
                modifier = Modifier.fillMaxSize()
            )
            ScanFrameOverlay(modifier = Modifier.fillMaxSize())
        } else {
            QuietStateCard(
                kind = QuietStateKind.CameraPermission,
                onPrimaryAction = { permissionLauncher.launch(Manifest.permission.CAMERA) },
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
private fun ScanFrameOverlay(modifier: Modifier = Modifier) {
    val overlayColor = Color.Black.copy(alpha = 0.25f)
    BoxWithConstraints(modifier = modifier) {
        val scanSize = if (maxWidth < 340.dp) maxWidth - 48.dp else 292.dp
        val frameColor = MaterialTheme.colorScheme.surfaceVariant
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frame = calculateScanFrame(
                containerWidthPx = size.width,
                containerHeightPx = size.height,
                density = density
            )
            val cornerRadius = 18.dp.toPx()
            val frameRect = Rect(
                offset = Offset(frame.left, frame.top),
                size = Size(frame.size, frame.size)
            )
            val overlayPath = Path().apply {
                addRect(Rect(Offset.Zero, size))
                addRoundRect(
                    RoundRect(
                        rect = frameRect,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                )
                fillType = PathFillType.EvenOdd
            }
            drawPath(path = overlayPath, color = overlayColor)
            drawRoundRect(
                color = frameColor,
                topLeft = frameRect.topLeft,
                size = frameRect.size,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Text(
            text = "4CutPhoto",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 34.dp, end = 22.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "QR을 사각형 안에 맞추고 잠시 멈춰주세요.",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = scanSize + 40.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "초점이 맞지 않으면 화면을 한 번 눌러주세요.",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = scanSize + 36.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(116.dp)
                .height(2.dp)
                .background(frameColor)
        )
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
    focusPolicy: ScanFocusPolicy,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }
    var isProcessing by remember { mutableStateOf(false) }
    var lastAcceptedValue by remember { mutableStateOf<String?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var lastFocusRequestMillis by remember { mutableStateOf<Long?>(null) }
    var manualFocusTarget by remember { mutableStateOf<FocusTarget?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(previewView, camera, isProcessing) {
        val boundPreviewView = previewView ?: return@LaunchedEffect
        val boundCamera = camera ?: return@LaunchedEffect
        delay(350L)
        while (!isProcessing) {
            val frame = calculateScanFrame(
                containerWidthPx = boundPreviewView.width.toFloat(),
                containerHeightPx = boundPreviewView.height.toFloat(),
                density = context.resources.displayMetrics.density
            )
            val now = System.currentTimeMillis()
            if (focusPolicy.shouldRequestAutoFocus(now, lastFocusRequestMillis, isProcessing)) {
                requestFocusAndMetering(
                    previewView = boundPreviewView,
                    camera = boundCamera,
                    target = frame.center
                )
                lastFocusRequestMillis = now
            }
            delay(400L)
        }
    }

    LaunchedEffect(manualFocusTarget, previewView, camera, isProcessing) {
        val requestedTarget = manualFocusTarget ?: return@LaunchedEffect
        val boundPreviewView = previewView ?: return@LaunchedEffect
        val boundCamera = camera ?: return@LaunchedEffect
        val now = System.currentTimeMillis()
        if (focusPolicy.canRequestManualFocus(now, lastFocusRequestMillis, isProcessing)) {
            requestFocusAndMetering(
                previewView = boundPreviewView,
                camera = boundCamera,
                target = requestedTarget
            )
            lastFocusRequestMillis = now
        }
        manualFocusTarget = null
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                previewView = this
                setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        manualFocusTarget = FocusTarget(event.x, event.y)
                    }
                    true
                }
            }
        },
        update = { currentPreviewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(currentPreviewView.surfaceProvider)
                    }
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
                    camera = cameraProvider.bindToLifecycle(
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

private fun requestFocusAndMetering(
    previewView: PreviewView,
    camera: Camera,
    target: FocusTarget
) {
    val point = previewView.meteringPointFactory.createPoint(target.x, target.y)
    val action = FocusMeteringAction.Builder(
        point,
        FocusMeteringAction.FLAG_AF or
            FocusMeteringAction.FLAG_AE or
            FocusMeteringAction.FLAG_AWB
    )
        .setAutoCancelDuration(3, TimeUnit.SECONDS)
        .build()
    camera.cameraControl.startFocusAndMetering(action)
}
