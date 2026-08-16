package com.uae.goldprice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: GoldPriceModel) : UiState()
    data class Error(val message: String) : UiState()
}

class GoldViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _history = MutableStateFlow(HistoryStore.read(application))
    val history: StateFlow<List<HistoryPoint>> = _history

    init {
        loadCachedData()
        fetchGoldPrice()
    }

    private fun loadCachedData() {
        val prefs = getApplication<Application>().getSharedPreferences("gold_prefs", 0)
        val cachedPrice = prefs.getString("cached_response", null)?.toDoubleOrNull() ?: return
        val ounceToGram = 31.1034768
        val gramPrice = cachedPrice / ounceToGram
        _uiState.value = UiState.Success(
            GoldPriceModel(
                karat24 = gramPrice,
                karat22 = gramPrice * 22.0 / 24.0,
                karat21 = gramPrice * 21.0 / 24.0,
                karat18 = gramPrice * 18.0 / 24.0,
                updatedAt = prefs.getString("cached_updated_at", "") ?: ""
            )
        )
    }

    fun fetchGoldPrice() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = RetrofitClient.instance.getGoldPrice()
                val ounceToGram = 31.1034768
                val pricePerGram24k = response.price / ounceToGram
                val model = GoldPriceModel(
                    karat24 = pricePerGram24k,
                    karat22 = pricePerGram24k * (22.0 / 24.0),
                    karat21 = pricePerGram24k * (21.0 / 24.0),
                    karat18 = pricePerGram24k * (18.0 / 24.0),
                    updatedAt = response.updatedAt
                )
                _history.value = HistoryStore.append(
                    getApplication(),
                    HistoryPoint(System.currentTimeMillis(), response.price)
                )
                getApplication<Application>().getSharedPreferences("gold_prefs", 0)
                    .edit().putString("cached_response", response.price.toString())
                    .putString("cached_updated_at", response.updatedAt).apply()
                _uiState.value = UiState.Success(model)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
