package diploma.teams

import diploma.constants.server.QUALITY_LOW
import diploma.constants.server.WIDTH_NARROW

data class PlayerConfig(val initialX: Int = 0, val initialY: Int = 0, var initialTurnNeck: Int = 0,
                        var kickPower: Int = 0, var kickDirection: Int = 0, var dashPower: Int = 0,
                        var turnNeck: Int = 90,
                        var viewWidth: String = WIDTH_NARROW, var viewQuality: String = QUALITY_LOW)