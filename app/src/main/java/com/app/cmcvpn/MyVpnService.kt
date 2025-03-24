package com.app.cmcvpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class MyVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private val servers = mapOf(
        "France" to "fr.vpn.server",
        "USA" to "us.vpn.server",
        "Germany" to "de.vpn.server"
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val country = intent?.getStringExtra("COUNTRY") ?: "France"
        startForeground(1, createNotification())
        startVpn(servers[country] ?: "fr.vpn.server")
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "vpn_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "VPN Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("VPN Active")
            .setContentText("Your VPN is running")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startVpn(serverAddress: String) {
        val builder = Builder()
        builder.addAddress("10.0.0.2", 24)
        builder.addRoute("0.0.0.0", 0)
        builder.setSession("MyVPN")
        builder.addDnsServer("8.8.8.8")
        builder.addDnsServer("8.8.4.4")
        vpnInterface = builder.establish()

        Thread {
            vpnInterface?.fileDescriptor?.let { fd ->
                val input = FileInputStream(fd)
                val output = FileOutputStream(fd)
                val packet = ByteBuffer.allocate(32767)
                val tunnel = DatagramChannel.open()
                tunnel.configureBlocking(false)
                tunnel.connect(InetSocketAddress(InetAddress.getByName(serverAddress), 1194))
                while (true) {
                    packet.clear()
                    val length = input.read(packet.array())
                    if (length > 0) {
                        tunnel.write(packet)
                    }
                }
            }
        }.start()
    }

    override fun onDestroy() {
        vpnInterface?.close()
        stopForeground(true)
        super.onDestroy()
    }
}