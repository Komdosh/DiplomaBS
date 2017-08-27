package model

data class VisiblePlayer(private val teamName: String?, private val playerNumber: Int, private val direction: Int?,
                         private val distance: Int?, private val ext: VisiblePlayerExtInfo?, private val tick: Int?)