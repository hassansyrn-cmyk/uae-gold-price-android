package com.uae.goldprice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class GoldPriceWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val prefs = appContext.getSharedPreferences("gold_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        return try {
            val response = RetrofitClient.instance.getGoldPrice()
            val currentOunceUsd = response.price
            HistoryStore.append(
                applicationContext,
                HistoryPoint(System.currentTimeMillis(), currentOunceUsd)
            )
            checkAndNotify(currentOunceUsd)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkAndNotify(currentPriceUsd: Double) {
        checkCustomAlerts(currentPriceUsd)
        val previousPrice = prefs.getFloat("last_ounce_usd", 0f).toDouble()
        val now = System.currentTimeMillis()
        val lastDailyNotification = prefs.getLong("last_daily_notification", 0L)
        val lastLargeAlert = prefs.getLong("last_large_alert", 0L)

        if (previousPrice > 0) {
            val percentChange = abs(currentPriceUsd - previousPrice) / previousPrice * 100.0
            val oneHour = TimeUnit.HOURS.toMillis(1)
            if (percentChange >= 10.0 && now - lastLargeAlert >= oneHour) {
                sendNotification(
                    R.string.notification_title_alert,
                    applicationContext.getString(
                        R.string.notification_body_large_change,
                        "%.2f".format(currentPriceUsd),
                        "%.1f".format(percentChange)
                    ),
                    2
                )
                prefs.edit().putLong("last_large_alert", now).apply()
            }
        }

        if (now - lastDailyNotification >= TimeUnit.DAYS.toMillis(1)) {
            sendNotification(
                R.string.notification_title_daily,
                applicationContext.getString(
                    R.string.notification_body_daily_ounce,
                    "%.2f".format(currentPriceUsd)
                ),
                1
            )
            prefs.edit().putLong("last_daily_notification", now).apply()
        }

        prefs.edit().putFloat("last_ounce_usd", currentPriceUsd.toFloat()).apply()
    }

    private fun checkCustomAlerts(currentPriceUsd: Double) {
        PriceAlertStore.read(applicationContext)
            .filter { it.enabled }
            .forEach { alert ->
                val reached = if (alert.directionAbove) {
                    currentPriceUsd >= alert.targetUsd
                } else {
                    currentPriceUsd <= alert.targetUsd
                }
                if (reached) {
                    sendNotification(
                        R.string.notification_title_alert,
                        applicationContext.getString(
                            R.string.alert_triggered_body,
                            "%.2f".format(currentPriceUsd)
                        ),
                        (alert.id % Int.MAX_VALUE).toInt()
                    )
                    PriceAlertStore.disable(applicationContext, alert.id)
                }
            }
    }

    private fun sendNotification(titleRes: Int, body: String, notificationId: Int) {
        val context = applicationContext
        val channelId = "gold_price_updates"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(titleRes))
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<GoldPriceWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "GoldPriceUpdate",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
