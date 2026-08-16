package com.uae.goldprice

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Stores hourly ounce snapshots so the chart works without a second paid history API. */
object HistoryStore {
    private const val PREFS = "gold_history"
    private const val KEY_POINTS = "ounce_points"
    private const val MAX_POINTS = 24 * 31

    fun read(context: Context): List<HistoryPoint> {
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

    @Synchronized
    fun append(context: Context, point: HistoryPoint): List<HistoryPoint> {
        val points = (read(context) + point)
            .distinctBy { it.time / (15 * 60 * 1000L) }
            .sortedBy { it.time }
            .takeLast(MAX_POINTS)
        val array = JSONArray()
        points.forEach {
            array.put(JSONObject().apply {
                put("time", it.time)
                put("price", it.price)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_POINTS, array.toString()).apply()
        return points
    }
}

data class HistoryPoint(val time: Long, val price: Double)

enum class ChartRange(val durationMillis: Long) {
    HOUR(60 * 60 * 1000L),
    DAY(24 * 60 * 60 * 1000L),
    WEEK(7 * 24 * 60 * 60 * 1000L),
    MONTH(31 * 24 * 60 * 60 * 1000L)
}
