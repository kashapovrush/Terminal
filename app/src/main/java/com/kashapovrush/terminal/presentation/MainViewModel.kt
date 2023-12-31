package com.kashapovrush.terminal.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kashapovrush.terminal.data.ApiFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _state = MutableStateFlow<TerminalScreenState>(TerminalScreenState.Initial)
    val state = _state.asStateFlow()

    private val apiService = ApiFactory.apiService

    private val exceptionHandler = CoroutineExceptionHandler {_, throwable ->
        Log.d("ExceptionHandler", "exception $throwable")
    }

    init {
        loadBars()
    }

    fun loadBars(timeFrame: TimeFrame = TimeFrame.HOUR_1) {
        _state.value = TerminalScreenState.Loading
        viewModelScope.launch(exceptionHandler) {
            val results = apiService.loadBar(timeFrame.value).results
            _state.value = TerminalScreenState.Content(results = results, timeFrame)
        }
    }
}