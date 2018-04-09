package diploma.model

import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth

data class SenseBody(private val viewQuality: ViewQuality,
                     private val viewWidth: ViewWidth,
                     private val stamina: String,
                     private val effort: String,
                     private val headAngle: String,
                     private val kick: String,
                     private val dash: String,
                     private val turn: String,
                     private val say: String,
                     private val turnNeck: String,
                     private val catch: String,
                     private val move: String,
                     private val changeView: String) {
}