package model

data class VisiblePlayer(val teamName: String?, val playerNumber: Int, val direction: Int?,
                         private val distance: Int?, private val ext: VisiblePlayerExtInfo?, private val tick: Int?)