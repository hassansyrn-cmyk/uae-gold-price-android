package com.uae.goldprice

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Merges the bundled LBMA daily seed with live hourly snapshots. */
object HistoryStore {
    private const val PREFS = "gold_history"
    private const val KEY_POINTS = "ounce_points"
    private const val KEY_SEED_VERSION = "lbma_seed_version"
    private const val SEED_VERSION = 1
    private const val MAX_POINTS = 2000

    fun read(context: Context): List<HistoryPoint> {
        ensureSeeded(context)
        return readStored(context)
    }

    @Synchronized
    fun append(context: Context, point: HistoryPoint): List<HistoryPoint> {
        ensureSeeded(context)
        val points = (readStored(context) + point)
            .distinctBy { it.time / (15 * 60 * 1000L) }
            .sortedBy { it.time }
            .takeLast(MAX_POINTS)
        persist(context, points)
        return points
    }

    private fun ensureSeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getInt(KEY_SEED_VERSION, 0) == SEED_VERSION) return
        val seeded = runCatching {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            context.assets.open("lbma_gold_am_usd_daily.csv").bufferedReader().useLines { lines ->
                lines.drop(1).mapNotNull { line ->
                    val parts = line.split(',')
                    if (parts.size != 2) return@mapNotNull null
                    val time = dateFormat.parse(parts[0])?.time ?: return@mapNotNull null
                    val price = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                    HistoryPoint(time, price)
                }.toList()
            }
        }.getOrDefault(emptyList())
        if (seeded.isNotEmpty()) {
            persist(context, seeded.takeLast(MAX_POINTS))
            prefs.edit().putInt(KEY_SEED_VERSION, SEED_VERSION).apply()
        }
    }

    private fun readStored(context: Context): List<HistoryPoint> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_POINTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(HistoryPoint(item.getLong("time"), item.getDouble("price")))
                }
            }.sortedBy { it.time }
        }.getOrDefault(emptyList())
    }

    private fun persist(context: Context, points: List<HistoryPoint>) {
        val array = JSONArray()
        points.forEach {
            array.put(JSONObject().apply {
                put("time", it.time)
                put("price", it.price)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_POINTS, array.toString()).apply()
    }
}

data class HistoryPoint(val time: Long, val price: Double)

enum class ChartRange(val durationMillis: Long) {
    HOUR(60 * 60 * 1000L),
    DAY(24 * 60 * 60 * 1000L),
    WEEK(7 * 24 * 60 * 60 * 1000L),
    MONTH(31 * 24 * 60 * 60 * 1000L),
    YEAR(365 * 24 * 60 * 60 * 1000L)
}
