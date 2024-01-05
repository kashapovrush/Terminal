package com.kashapovrush.terminal.presentation

import com.kashapovrush.terminal.data.Bar

sealed class TerminalScreenState {

    object Initial: TerminalScreenState()

    object Loading: TerminalScreenState()

    data class Content(val results: List<Bar>, val timeFrame: TimeFrame): TerminalScreenState()
}
