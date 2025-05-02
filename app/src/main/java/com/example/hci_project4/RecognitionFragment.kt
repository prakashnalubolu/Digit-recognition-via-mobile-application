package com.example.hci_project3

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hci_project3.databinding.FragmentRecognitionBinding
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecognitionFragment : Fragment() {
    private var _binding: FragmentRecognitionBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var textRecognizer: TextRecognizer? = null

    private var frameCount = 0
    private var lastFPSTimestamp = System.currentTimeMillis()
    private var fps = 0f

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            context?.let {
                Toast.makeText(it, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
            closeFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            Log.d(TAG, "onCreate called")
            context?.let {
                try {
                    // Ensure that the TextRecognizer is the updated version that uses Module.load().
                    textRecognizer = TextRecognizer(it)
                    Log.d(TAG, "TextRecognizer created successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize TextRecognizer", e)
                    Toast.makeText(context, "Error initializing recognition: ${e.message}", Toast.LENGTH_LONG).show()
                    closeFragment()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            closeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            cameraExecutor = Executors.newSingleThreadExecutor()
            setupUI()
            checkCameraPermission()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            context?.let {
                Toast.makeText(it, "Error setting up camera", Toast.LENGTH_SHORT).show()
            }
            closeFragment()
        }
    }

    private fun setupUI() {
        binding.closeButton.setOnClickListener {
            closeFragment()
        }
    }

    private fun checkCameraPermission() {
        when {
            allPermissionsGranted() -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                context?.let {
                    Toast.makeText(it, "Camera permission is required for this feature", Toast.LENGTH_LONG).show()
                }
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun closeFragment() {
        activity?.supportFragmentManager?.popBackStack()
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val context = context ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(cameraProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting camera", e)
                Toast.makeText(context, "Failed to start camera", Toast.LENGTH_SHORT).show()
                closeFragment()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        if (!isAdded || _binding == null) return

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(imageProxy)
                }
            }

        try {
            cameraProvider.unbindAll()

            // Try to use back camera, fallback to any available camera
            val cameraSelector = try {
                CameraSelector.DEFAULT_BACK_CAMERA
            } catch (e: Exception) {
                Log.w(TAG, "Back camera not available, trying to use any camera", e)
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
            context?.let {
                Toast.makeText(it, "Failed to bind camera", Toast.LENGTH_SHORT).show()
            }
            closeFragment()
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxy.toBitmap()
            updateFPS()
            
            textRecognizer?.let { recognizer ->
                val result = recognizer.recognize(bitmap)
                activity?.runOnUiThread {
                    if (_binding != null) {
                        binding.recognitionResult.text = "Recognition: ${result.text}"
                        binding.latencyResult.text = "Latency: ${result.latencyMs}ms"
                        binding.fpsCounter.text = "FPS: %.1f".format(fps)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun updateFPS() {
        frameCount++
        val now = System.currentTimeMillis()
        val delta = now - lastFPSTimestamp
        if (delta > 1000) {
            fps = frameCount * 1000f / delta
            frameCount = 0
            lastFPSTimestamp = now
        }
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            cameraExecutor.shutdown()
            _binding = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroyView", e)
        }
    }

    companion object {
        private const val TAG = "RecognitionFragment"
    }
}
