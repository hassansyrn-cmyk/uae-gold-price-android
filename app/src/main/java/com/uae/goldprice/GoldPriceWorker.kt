package com.uae.goldprice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class GoldPriceWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val prefs = appContext.getSharedPreferences("gold_prefs", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        return try {
            val response = RetrofitClient.instance.getGoldPrice()
            val ounceToGram = 31.1034768
            val pricePerGram24k = response.price / ounceToGram
            val currentPriceAed = pricePerGram24k * 3.6725

            checkAndNotify(currentPriceAed)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkAndNotify(currentPrice: Double) {
        val lastPrice = prefs.getFloat("last_price", 0f).toDouble()
        val lastNotificationTime = prefs.getLong("last_notification_time", 0L)
        val currentTime = System.currentTimeMillis()

        var shouldNotify = false
        var titleRes = R.string.notification_title_daily

        if (lastPrice > 0) {
            val priceDiff = abs(currentPrice - lastPrice)
            val percentChange = (priceDiff / lastPrice) * 100

            if (percentChange >= 1.0 || priceDiff >= 5.0) {
                shouldNotify = true
                titleRes = R.string.notification_title_alert
            }
        }

        // If no volatility alert, check if 24 hours passed for daily update
        if (!shouldNotify) {
            val twentyFourHoursMs = TimeUnit.DAYS.toMillis(1)
            if (currentTime - lastNotificationTime >= twentyFourHoursMs) {
                shouldNotify = true
                titleRes = R.string.notification_title_daily
            }
        }

        if (shouldNotify) {
            sendNotification(currentPrice, titleRes)
            prefs.edit()
                .putFloat("last_price", currentPrice.toFloat())
                .putLong("last_notification_time", currentTime)
                .apply()
        }
    }

    private fun sendNotification(priceAed: Double, titleRes: Int) {
        val context = applicationContext
        val channelId = "gold_price_updates"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Gold Price Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(R.string.notification_body, "%.2f".format(priceAed)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
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
