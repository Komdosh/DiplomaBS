package diploma.model

data class VisiblePlayer(val teamName: String?, val playerNumber: Int, val direction: Int?,
                         val distance: Int?, val ext: VisiblePlayerExtInfo?, private val tick: Int?)