package com.kashapovrush.terminal.presentation

import com.kashapovrush.terminal.data.Bar

sealed class TerminalScreenState {

    object Initial: TerminalScreenState()

    data class Content(val results: List<Bar>): TerminalScreenState()
}
