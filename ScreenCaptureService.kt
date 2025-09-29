package com.example.socketclient

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null

    // Handler thread for image processing (so UI thread is not used)
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    // Small queue to decouple image capture from network sending
    private val frameQueue = LinkedBlockingQueue<ByteArray>(8)
    private var senderThread: Thread? = null

    // Server info (same as you gave)
    private val SERVER_IP = "192.168.43.27"
    private val SERVER_PORT = 4444

    // Throttle: send at most one frame every FRAME_INTERVAL_MS (default 200ms -> 5 FPS)
    private val FRAME_INTERVAL_MS = 200L
    private var lastFrameTime = 0L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        val resultData: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("mediaProjectionData", Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("mediaProjectionData")
        }

        if (resultData != null) {
            val projectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            mediaProjection = projectionManager.getMediaProjection(Activity.RESULT_OK, resultData)

            if (mediaProjection != null) {
                startHandlerThread()      // for image processing
                startSenderThread()       // for network sending and reconnects
                startScreenCapture()      // start capturing frames
            } else {
                Log.e(TAG, "Failed to get MediaProjection")
            }
        } else {
            Log.w(TAG, "Result data is null or missing")
        }

        return START_STICKY
    }

    private fun startHandlerThread() {
        handlerThread = HandlerThread("ImageListenerThread").apply { start() }
        handler = Handler(handlerThread!!.looper)
    }

    private fun startSenderThread() {
        senderThread = Thread {
            var socket: Socket? = null
            var out: OutputStream? = null

            while (!Thread.currentThread().isInterrupted) {
                try {
                    // ensure connected
                    if (socket == null || socket.isClosed || !socket.isConnected) {
                        try {
                            socket = Socket()
                            socket.connect(InetSocketAddress(SERVER_IP, SERVER_PORT), 5000)
                            out = socket.getOutputStream()
                            Log.d(TAG, "Connected to server $SERVER_IP:$SERVER_PORT")
                        } catch (e: Exception) {
                            Log.e(TAG, "Connect failed: ${e.message}")
                            socket?.close()
                            socket = null
                            out = null
                            Thread.sleep(2000) // retry delay
                            continue
                        }
                    }

                    // get frame (wait up to 500ms)
                    val frame = frameQueue.poll(500, TimeUnit.MILLISECONDS) ?: continue

                    try {
                        // Protocol: send ASCII size + newline, then raw JPEG bytes
                        val sizeBytes = frame.size.toString().toByteArray(Charsets.US_ASCII)
                        out?.write(sizeBytes)
                        out?.write('\n'.code)
                        out?.write(frame)
                        out?.flush()
                        Log.d(TAG, "Frame sent: ${frame.size} bytes")
                    } catch (e: Exception) {
                        Log.e(TAG, "Send failed: ${e.message}")
                        // close and attempt reconnect
                        socket?.close()
                        socket = null
                        out = null
                        // try to requeue the frame (if queue not full)
                        frameQueue.offer(frame)
                        Thread.sleep(1000)
                    }
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    Log.e(TAG, "Sender thread unexpected error: ${e.message}")
                }
            }

            try { socket?.close() } catch (_: Exception) {}
        }
        senderThread!!.start()
    }

    private fun startScreenCapture() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val screenDensity = metrics.densityDpi

        Log.d(TAG, "Screen size: ${width}x$height, density: $screenDensity")

        // create ImageReader (RGBA_8888)
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            screenDensity,
            android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        // Use the handler associated with handlerThread so processing is off the UI thread.
        imageReader?.setOnImageAvailableListener({ reader ->
            val now = System.currentTimeMillis()
            if (now - lastFrameTime < FRAME_INTERVAL_MS) {
                // throttle: drop the frame if it's too soon
                val imgToDrop = reader.acquireLatestImage()
                imgToDrop?.close()
                return@setOnImageAvailableListener
            }
            lastFrameTime = now

            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener

            // Processing happens on the handler thread because we supplied 'handler'
            try {
                // Many devices provide a single plane for RGBA_8888
                val plane = image.planes[0]
                val buffer: ByteBuffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * width

                // Create a temporary bitmap that accounts for row padding (common pattern).
                // We will crop it back to the real width/height afterwards.
                val bitmapWidth = if (pixelStride != 0) width + rowPadding / pixelStride else width

                val bmp: Bitmap? = try {
                    buffer.rewind()
                    val tmp = Bitmap.createBitmap(bitmapWidth, height, Bitmap.Config.ARGB_8888)
                    tmp.copyPixelsFromBuffer(buffer)           // may throw on some devices - caught below
                    // crop to the real width/height
                    Bitmap.createBitmap(tmp, 0, 0, width, height).also { tmp.recycle() }
                } catch (e: Exception) {
                    Log.e(TAG, "Bitmap creation failed: ${e.message}")
                    null
                }

                if (bmp != null) {
                    // Compress to JPEG on the background thread
                    val baos = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos) // 50% quality
                    val bytes = baos.toByteArray()
                    baos.close()

                    // Offer to the queue (non-blocking). If the queue is full we drop the frame.
                    if (!frameQueue.offer(bytes)) {
                        Log.w(TAG, "Frame queue full, dropping frame")
                    }
                    bmp.recycle()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}")
            } finally {
                image.close()
            }

        }, handler)
    }

    override fun onDestroy() {
        try {
            handlerThread?.quitSafely()
            handlerThread?.join(500)
        } catch (_: Exception) {}
        try {
            senderThread?.interrupt()
            senderThread?.join(500)
        } catch (_: Exception) {}
        virtualDisplay?.release()
        imageReader?.close()
        try {
            mediaProjection?.stop()
        } catch (_: Exception) {}
        mediaProjection = null
        frameQueue.clear()
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "ScreenCaptureService"
    }
}
