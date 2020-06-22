package com.gjung.haifa3d.ble

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.gjung.haifa3d.R
import com.gjung.haifa3d.notification.createBatteryLevelNotificationChannel

class BleService : NotificationService() {
    lateinit var manager: AppBleManager

    // Binder given to clients
    private val binder = LocalBinder()
    private var isObserving = false
    private var batteryLowNotificationId: Int? = null

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BleService = this@BleService
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent!!)
        return binder
    }

    private fun setNotificationText(text: CharSequence) {
        var n = notificationBuilder
            .setContentText(text)
            .build()
        updateNotification(n)
    }

    private fun setBatteryPercentageText(percentage: Int) =
        setNotificationText("$percentage % battery")

    private fun onBatteryNotification(notification: BatteryNotification) {
        setBatteryPercentageText(notification.percentage)
        var id = batteryLowNotificationId
        if (notification.percentage > 20 && id != null) {
            batteryLowNotificationId = null
            notificationManager.cancel(id)
        } else if (notification.percentage <= 20 && batteryLowNotificationId == null) {
            id = notificationId + 1
            batteryLowNotificationId = id
            val b = prepareNotificationBuilder(notificationManager.createBatteryLevelNotificationChannel(this))
            val msg = getString(R.string.notification_battery_low_content, notification.percentage)
            val tit = getText(R.string.notification_battery_low_title)
            b.setContentTitle(tit)
            b.setContentText(msg)
            b.setTicker(msg)
            notificationManager.notify(id, b.build())
        }
    }

    override fun onCreate() {
        super.onCreate()
        manager = AppBleManager(this)
        manager.state.observe(this, Observer {
            if (!isObserving && it.isConnected) {
                manager.batteryService.currentPercentage.observe(this@BleService, Observer {
                    it?.let { onBatteryNotification(it) }
                })
                isObserving = true
            } else if (!it.isConnected) {
                setNotificationText("Not connected")
            }
        })
    }

    override fun onDestroy() {
        manager.close()
        super.onDestroy()
    }
}