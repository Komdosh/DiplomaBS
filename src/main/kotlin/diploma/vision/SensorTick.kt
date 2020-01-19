package diploma.vision

import diploma.constants.server.TICK
import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth
import kotlin.math.ceil
import kotlin.math.floor

data class SensorTick(
        val tickService: TickService,
        var withoutMainObject: Int = 0,
        var forGettingMinimalQualityInfo: Int = 0,
        var estimateWithoutMainObject: Int = 0) {

    fun estimateWithoutMainObject(viewWidth: ViewWidth, maxVisibleAngle: Double = 0.0): Int {
        estimateWithoutMainObject = 0
        if (withoutMainObject > 0) {
            val viewAngle = getViewAngle(viewWidth)
            estimateWithoutMainObject = floor(maxVisibleAngle / viewAngle / 2).toInt() //2 is a magic coefficient for now
        }
        return estimateWithoutMainObject
    }


    fun calculateWithoutMainObject(viewWidth: ViewWidth, viewQuality: ViewQuality,
                                   maxVisibleAngle: Double = 0.0): Int {
        val viewAngle = getViewAngle(viewWidth)
        withoutMainObject = 0
        if (viewAngle < maxVisibleAngle) {
            val angleWithoutMainObject = ceil((maxVisibleAngle - viewAngle) / viewAngle)
            val sensorFrequency = getViewFrequency(viewWidth, viewQuality) / TICK
            withoutMainObject = (sensorFrequency * angleWithoutMainObject).toInt()
            val sensorTickUntilAction = countUntilAction(viewWidth, viewQuality)
            if (withoutMainObject > sensorTickUntilAction) {
                withoutMainObject = sensorTickUntilAction
            }
        }
        return withoutMainObject
    }

    fun calculateForGetMinimalQualityInfo(maxVisibleAngle: Double = 0.0, currentViewWidthForLow: ViewWidth): Int {
        forGettingMinimalQualityInfo = ceil(maxVisibleAngle / getViewAngle(currentViewWidthForLow) *
                getViewFrequency(currentViewWidthForLow, ViewQuality.LOW) / TICK).toInt() + 1
        return forGettingMinimalQualityInfo
    }

    fun countUntilAction(viewWidth: ViewWidth, viewQuality: ViewQuality): Int =
            floor(tickService.getUntilAction() * TICK / getViewFrequency(viewWidth, viewQuality)).toInt()
}

