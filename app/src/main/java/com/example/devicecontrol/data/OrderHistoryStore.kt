package com.example.devicecontrol.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class OrderHistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("order_history", Context.MODE_PRIVATE)
    private val adapter = Moshi.Builder()
        .add(LenientStringJsonAdapter())
        .build()
        .adapter<List<OrderHistoryItem>>(
            Types.newParameterizedType(List::class.java, OrderHistoryItem::class.java),
        )

    fun list(): List<OrderHistoryItem> {
        val raw = prefs.getString(KEY_ORDERS, null) ?: return emptyList()
        return runCatching { adapter.fromJson(raw).orEmpty() }.getOrDefault(emptyList())
    }

    fun add(item: OrderHistoryItem) {
        val next = (list().filterNot { it.orderNo == item.orderNo } + item)
            .sortedByDescending { it.completedAt }
            .take(MAX_HISTORY)
        prefs.edit().putString(KEY_ORDERS, adapter.toJson(next)).apply()
    }

    private companion object {
        const val KEY_ORDERS = "orders"
        const val MAX_HISTORY = 50
    }
}
