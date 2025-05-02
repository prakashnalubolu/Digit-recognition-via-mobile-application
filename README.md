**Real-Time Digit Recognition Android App**
![image](https://github.com/user-attachments/assets/39d27ca3-5637-45b1-bb6e-645550935eda)

**Overview**

This repository brings together two core components of a complete digit recognition pipeline:

- **Model Training** – A Convolutional Recurrent Neural Network (CRNN) built and trained using PyTorch, designed to recognize digit from images with Connectionist Temporal Classification (CTC) decoding.
- **Android Deployment** – An Android app leveraging PyTorch Mobile and CameraX to perform on-device, real-time digit recognition, with low latency and offline capabilities.
By combining these modules, you can train a custom digit recognition model for your own dataset and immediately deploy it to a mobile device.CTC) for decoding, this app delivers low-latency, offline digit recognition suitable for a variety of use cases—from quick digit/text scanning to immersive augmented reality overlays.

**Key Features**

* **On-Device Inference**: Run digit recognition entirely on the device—no internet connection required.
* **Real-Time Performance**: Capture and process camera frames at interactive frame rates, displaying recognized digit with live FPS and latency indicators.
* **Robust Preprocessing Pipeline**: Convert raw YUV camera frames into normalized grayscale tensors, ensuring consistent input for the CRNN model.
* **CTC Decoding**: Employ greedy and beam-search decoding strategies to translate model logits into human-readable digit.
* **User-Friendly UI**: Intuitive layout with a live camera preview, recognition overlay, and performance metrics; includes a start/stop recognize toggle.
* **Modular Architecture**: Three clearly separated modules (Camera, Recognition, UI) facilitate maintainability and extensibility.
* **Performance Optimization**: Optional support for model quantization, multi-threaded execution, and bitmap pooling to maximize speed and reduce memory footprint.

## Project Explanation

* **Training Pipeline**: Preprocess images and labels, train a Convolutional Recurrent Neural Network (CRNN) with CTC loss in PyTorch, and evaluate using Word/Character Error Rates.
* **Mobile App**: Integrate the trained model into an Android app via PyTorch Mobile and CameraX for offline, real-time OCR with live digit overlays.

## Model Architecture

### 1. Custom CNN (Task 1)

* **Convolutional Layers**: Three Conv2D layers with filter sizes \[32, 64, 128], each followed by BatchNorm, ReLU, and MaxPooling (2×2) to progressively reduce spatial dimensions.
* **Fully Connected Layers**: After flattening, two Dense layers (256 units → 128 units) with ReLU activations and dropout (0.5) for regularization.
* **Output Layer**: Softmax activation over 10 classes (digits 0–9).
* **Parameters**: \~3.24 million total parameters, trained with CTC-compatible labeling but using cross-entropy for digit classification.

### 2. Fine-Tuned Pre-trained Models (Task 2)

* **ResNet50**: Replace final FC layer to output 10 classes; unfreeze top layers for fine-tuning on MNIST.
* **MobileNetV2**: Lightweight backbone adapted for digits with a new classification head; ideal for mobile inference.
* **VGG16**: Deep feature extractor with the top layers replaced; provides highest accuracy (\~98.6%).

### 3. Video-Based Recognition (Task 3)

* **Frame Extraction**: Select key frames from input video using OpenCV.
* **Preprocessing**: Grayscale conversion, adaptive thresholding, resizing to 28×28 px.
* **Classification**: Use the best-performing CNN (CNN+Adam+L2) to predict digits in each frame.

#### Digits
The model recognizes ten classes (digits 0–9). The mapping is defined in `alphabet.txt`, listing each digit on its own line. During training, labels are encoded as integers 1–10; index 0 is reserved for a CTC blank token.


* **Config**: Batch size, learning rate, epochs set in `config.yaml`.
* **Output**: Checkpoints (`best_model.pt`) and TensorBoard logs in `runs/`.
* **Evaluation**: Use `evaluate.py` for WER/CER metrics.

## Android Deployment

This section details how to integrate and deploy your trained digit recognition model into an Android app for real-time on-device OCR.

1. **Environment Setup**

   * Android Studio (Arctic Fox or later) with Kotlin support.
   * Minimum SDK API level 24 for CameraX and PyTorch Mobile compatibility.
   * Include PyTorch Mobile AAR in `app/libs/` and add CameraX dependencies in `build.gradle`.

2. **Model Integration**

   * Copy `best_model.pt` into `app/src/main/assets/`.
   * Load the model in your `digitRecognizer` class using `Module.load()` from PyTorch Mobile.
   * Initialize SoLoader in `Application.onCreate()` or before model loading.

3. **Permissions & CameraX**

   * Request `CAMERA` permission at runtime with `ActivityResultContracts.RequestPermission`.
   * Configure CameraX with two use cases:

     * **Preview**: Display `PreviewView` in layout for live feed.
     * **Analysis**: Use `ImageAnalysis` with `STRATEGY_KEEP_ONLY_LATEST` to feed frames to your analyzer.
   * In `bindCameraUseCases()`, select `DEFAULT_BACK_CAMERA`, bind preview and analysis to the lifecycle owner.

4. **Frame Preprocessing**

   * Convert `ImageProxy` (YUV format) to `Bitmap`.
   * Resize or crop to model’s expected input height (e.g., 32 px), maintaining aspect ratio.
   * Convert to grayscale tensor normalized to \[-1, 1] with shape `[1, 1, H, W]`.

5. **Inference & Decoding**

   * Run `module.forward(IValue.from(tensor))` to obtain logits.
   * Use greedy CTC decoding: iterate over timesteps, pick highest-probability index, collapse repeats, skip blanks.
   * Measure inference latency by timestamping before/after `forward()`.

6. **UI Overlay**

   * Display recognized digit string on a transparent `digitView` overlaying the `PreviewView`.
   * Show FPS by counting frames in a time window and latency per inference.
   * Use `runOnUiThread` to update UI elements from background threads.
