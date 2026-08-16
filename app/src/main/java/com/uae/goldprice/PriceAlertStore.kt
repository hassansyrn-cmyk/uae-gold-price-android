package com.uae.goldprice

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PriceAlertStore {
    private const val PREFS = "price_alerts"
    private const val KEY_ALERTS = "alerts"

    fun read(context: Context): List<PriceAlert> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ALERTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        PriceAlert(
                            id = item.getLong("id"),
                            targetUsd = item.getDouble("targetUsd"),
                            directionAbove = item.getBoolean("directionAbove"),
                            enabled = item.optBoolean("enabled", true)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, targetUsd: Double, directionAbove: Boolean): List<PriceAlert> {
        val alerts = read(context) + PriceAlert(
            id = System.currentTimeMillis(),
            targetUsd = targetUsd,
            directionAbove = directionAbove,
            enabled = true
        )
        persist(context, alerts)
        return alerts
    }

    fun remove(context: Context, id: Long): List<PriceAlert> {
        val alerts = read(context).filterNot { it.id == id }
        persist(context, alerts)
        return alerts
    }

    fun disable(context: Context, id: Long): List<PriceAlert> {
        val alerts = read(context).map { if (it.id == id) it.copy(enabled = false) else it }
        persist(context, alerts)
        return alerts
    }

    private fun persist(context: Context, alerts: List<PriceAlert>) {
        val array = JSONArray()
        alerts.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("targetUsd", it.targetUsd)
                put("directionAbove", it.directionAbove)
                put("enabled", it.enabled)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_ALERTS, array.toString()).apply()
    }
}

data class PriceAlert(
    val id: Long,
    val targetUsd: Double,
    val directionAbove: Boolean,
    val enabled: Boolean
)
