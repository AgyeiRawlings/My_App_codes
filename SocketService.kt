package com.example.socketclient

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.net.Socket
import kotlin.concurrent.thread

class SocketService : Service() {

    private var socket: Socket? = null

    // Hardcoded IP and port
    private val SERVER_IP = "192.168.43.27"
    private val SERVER_PORT = 4444

    private val CHANNEL_ID = "socket_service_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(1, createNotification())

        connectToServer(SERVER_IP, SERVER_PORT)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Socket Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Socket Service Running")
            .setContentText("Connected to $SERVER_IP:$SERVER_PORT")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun connectToServer(ip: String, port: Int) {
        thread {
            try {
                socket = Socket(ip, port)
                // TODO: Implement your socket communication here
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Add reconnect logic if needed
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        socket?.close()
        super.onDestroy()
    }
}
