package com.example.socketclient

import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ScreenCaptureService", "Service started")

        if (intent != null && intent.hasExtra("mediaProjectionData")) {
            val resultData = intent.getParcelableExtra<Intent>("mediaProjectionData")
            val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(Activity.RESULT_OK, resultData!!)
            
            // TODO: Start virtual display capture here (surface/image reader)
            Log.d("ScreenCaptureService", "MediaProjection initialized")
        } else {
            Log.w("ScreenCaptureService", "No MediaProjection data received")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaProjection?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
