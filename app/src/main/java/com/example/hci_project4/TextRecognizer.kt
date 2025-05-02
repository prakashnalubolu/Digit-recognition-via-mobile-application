package com.example.hci_project3

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.facebook.soloader.SoLoader
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class TextRecognizer(private val context: Context) {
    private var module: Module? = null
    private val alphabet = "0123456789abcdefghijklmnopqrstuvwxyz"
    private val indexToChar = mutableMapOf<Int, Char>().apply {
        put(0, ' ') // blank
        alphabet.forEachIndexed { index, char ->
            put(index + 1, char)
        }
    }

    init {
        try {
            SoLoader.init(context, false)
            initializeModule()
            Log.d(TAG, "TextRecognizer initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextRecognizer", e)
            throw e
        }
    }

    private fun initializeModule() {
        try {
            val modelPath = assetFilePath(context, "sai_ts_CRNN.pt")
            module = Module.load(modelPath)
            Log.d(TAG, "Model loaded successfully from $modelPath")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load model", e)
            throw RuntimeException("Failed to load PyTorch model", e)
        }
    }

    fun recognize(bitmap: Bitmap): RecognitionResult {
        val startTime = System.nanoTime()
        return try {
            Log.d(TAG, "Starting recognition process")

            val tensor = preprocess(bitmap)
            Log.d(TAG, "Image preprocessed successfully")

            val output = module?.forward(IValue.from(tensor))?.toTensor()
            Log.d(TAG, "Model inference completed")

            val result = if (output != null) {
                val predictions = output.dataAsFloatArray
                Log.d(TAG, "Got predictions array of size: ${predictions.size}")
                ctcDecode(predictions)
            } else {
                "Error: No output from model"
            }

            val endTime = System.nanoTime()
            val latencyMs = (endTime - startTime) / 1_000_000
            Log.d(TAG, "Recognition completed in ${latencyMs}ms with result: $result")

            RecognitionResult(result, latencyMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error during recognition", e)
            RecognitionResult("Error: ${e.message}", 0)
        }
    }

    /**
     * Preprocess the bitmap to a 4D tensor [N, C, H, W] = [1, 1, H, W],
     * normalized to [-1, 1] following training conditions.
     */
    private fun preprocess(bitmap: Bitmap): Tensor {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val floatArray = FloatArray(width * height)
        for (i in pixels.indices) {
            val c = pixels[i]
            val r = ((c shr 16) and 0xFF) / 255.0f
            val g = ((c shr 8) and 0xFF) / 255.0f
            val b = (c and 0xFF) / 255.0f
            val gray = (r + g + b) / 3.0f
            // Normalize to [-1,1]
            val normalized = (gray - 0.5f) / 0.5f
            floatArray[i] = normalized
        }

        // The model expects shape: [N, C, H, W]
        // N = 1 (single image)
        // C = 1 (grayscale)
        // H = height
        // W = width
        return Tensor.fromBlob(floatArray, longArrayOf(1, 1, height.toLong(), width.toLong()))
    }

    private fun ctcDecode(logits: FloatArray): String {
        var previousChar = -1
        val decoded = StringBuilder()
        val sequenceLength = logits.size / (alphabet.length + 1)

        for (i in 0 until sequenceLength) {
            val offset = i * (alphabet.length + 1)
            var maxIndex = 0
            var maxValue = logits[offset]
            for (j in 1..alphabet.length) {
                if (logits[offset + j] > maxValue) {
                    maxValue = logits[offset + j]
                    maxIndex = j
                }
            }

            if (maxIndex != 0 && maxIndex != previousChar) { // 0 is blank
                decoded.append(indexToChar[maxIndex] ?: '?')
                previousChar = maxIndex
            }
        }

        return decoded.toString()
    }

    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
        }
        return file.absolutePath
    }

    companion object {
        private const val TAG = "TextRecognizer"
    }
}

data class RecognitionResult(
    val text: String,
    val latencyMs: Long
)
