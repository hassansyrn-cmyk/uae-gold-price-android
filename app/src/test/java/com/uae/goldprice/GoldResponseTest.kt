package com.uae.goldprice

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class GoldResponseTest {
    @Test
    fun apiResponsePreservesPriceTimestampAndCurrency() {
        val response = Gson().fromJson(
            """{"price":2675.25,"updatedAt":"2026-10-19T12:00:00Z","currency":"USD"}""",
            GoldResponse::class.java
        )
        assertEquals(2675.25, response.price, 0.00001)
        assertEquals("2026-10-19T12:00:00Z", response.updatedAt)
        assertEquals("USD", response.currency)
    }
}
