package diploma.vision

import diploma.constants.server.TICK
import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth
import java.lang.Math.ceil
import java.lang.Math.floor

class VisualSensorAlgorithm {

  private val boundAngel: Double = 90.0

  private var tickUntilAction: Int = 8 //hardcoded for now
  private var maxVisibleAngle: Double = 0.0
  private var sensorTickWithoutMainObject: Int = 0
  private var sensorTicksForGettingMinimalQualityInfo: Int = 0
  private var estimateSensorTickWithoutMainObject: Int = 0
  private var visualTickForEstimate: Int = 0
  private var estimateUsefulnessAngles: Int = 0

  constructor() {}

  fun countTickUntilAction(): Int {
    return tickUntilAction
  }

  fun serverTicked(): Int = --tickUntilAction

  fun countSensorTickUntilAction(viewWidth: ViewWidth, viewQuality: ViewQuality): Int =
      floor(tickUntilAction * TICK / getViewFrequency(viewWidth, viewQuality)).toInt()

  fun calculateSensorTicksForGetMinimalQualityInfo(): Int {
    sensorTicksForGettingMinimalQualityInfo = ceil(maxVisibleAngle / getViewAngle(ViewWidth.WIDE) *
        getViewFrequency(ViewWidth.WIDE, ViewQuality.LOW) / TICK).toInt()
    return sensorTicksForGettingMinimalQualityInfo
  }

  fun calculateMaxVisibleAngle(viewWidth: ViewWidth, viewQuality: ViewQuality): Double {
    val viewAngel = getViewAngle(viewWidth)
    val sightsCountForMaxAngle = getSightsCountForAngel(boundAngel, viewAngel)
    val sensorFrequency = getViewFrequency(viewWidth, viewQuality) / TICK
    val ticksForSeeMaxAngel = sightsCountForMaxAngle * sensorFrequency
    val sensorTickUntilAction = countSensorTickUntilAction(viewWidth, viewQuality)
    maxVisibleAngle = if (ticksForSeeMaxAngel > sensorTickUntilAction) {
      floor(sensorTickUntilAction / sensorFrequency) * viewAngel
    } else {
      boundAngel * 2.0
    }
    return maxVisibleAngle
  }

  fun calculateSensorTickWithoutMainObject(viewWidth: ViewWidth, viewQuality: ViewQuality): Int {
    val viewAngel = getViewAngle(viewWidth)
    sensorTickWithoutMainObject = 0
    if (viewAngel < maxVisibleAngle) {
      val angelWithoutMainObject = ceil((maxVisibleAngle - viewAngel) / viewAngel)
      val sensorFrequency = getViewFrequency(viewWidth, viewQuality) / TICK
      sensorTickWithoutMainObject = (sensorFrequency * angelWithoutMainObject).toInt()
      val sensorTickUntilAction = countSensorTickUntilAction(viewWidth, viewQuality)
      if (sensorTickWithoutMainObject > sensorTickUntilAction) {
        sensorTickWithoutMainObject = sensorTickUntilAction
      }
    }
    return sensorTickWithoutMainObject
  }

  private fun getSightsCountForAngel(maxAngel: Double, viewAngel: Double) = ceil((maxAngel * 2) / viewAngel)

  fun estimateSensorTickWithoutMainObject(viewWidth: ViewWidth): Int {
    estimateSensorTickWithoutMainObject = 0
    if (sensorTickWithoutMainObject > 0) {
      val viewAngel = getViewAngle(viewWidth)
      estimateSensorTickWithoutMainObject = floor(maxVisibleAngle / viewAngel / 2).toInt() //2 is a magic coefficient for now
    }
    return estimateSensorTickWithoutMainObject
  }

  fun getLowQualityVisualInfo() {

  }

  fun countVisualTickForEstimate(): Int {
    return 0
  }

  fun estimateUsefulnessAngles(): Int {
    return 0
  }

  fun getHighQualityVisualInfo() {

  }
}