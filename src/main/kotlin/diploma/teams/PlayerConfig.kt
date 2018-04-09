package diploma.teams

import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth

data class PlayerConfig(val initialX: Int = 0, val initialY: Int = 0, var initialTurnNeck: Int = 0,
                        var kickPower: Int = 0, var kickDirection: Int = 0, var dashPower: Int = 0,
                        var turnNeck: Int = 90,
                        var viewWidth: ViewWidth = ViewWidth.WIDE, var viewQuality: ViewQuality = ViewQuality.LOW)