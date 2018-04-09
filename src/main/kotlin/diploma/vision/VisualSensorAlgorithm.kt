package diploma.vision

import diploma.constants.server.TICK
import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth
import java.lang.Math.ceil
import java.lang.Math.floor

class VisualSensorAlgorithm {

  private var tickUntilAction: Int = 3 //hardcoded for now
  private var sensorTickUntilAction: Double = 0.0
  private var maxVisibleAngle: Double = 0.0
  private var sensorTickWithoutMainObject: Int = 0
  private var estimateSensorTickWithoutMainObject: Int = 0
  private var visualTickForEstimate: Int = 0
  private var estimateUsefulnessAngles: Int = 0

  constructor() {}

  fun countTickUntilAction(): Int {
    return tickUntilAction
  }

  fun countSensorTickUntilAction(viewWidth: ViewWidth, viewQuality: ViewQuality): Double {
    sensorTickUntilAction = tickUntilAction * TICK / getViewFrequency(viewWidth, viewQuality)
    return sensorTickUntilAction
  }

  fun calculateMaxVisibleAngle(viewWidth: ViewWidth, viewQuality: ViewQuality): Double {
    val boundAngle = 120
    val viewAngel = getViewAngle(viewWidth)
    val sightsForMaxAngle = ceil((boundAngle * 2) / viewAngel)
    val sensorFrequency = getViewFrequency(viewWidth, viewQuality) / TICK
    val ticksForSeeMaxAngel = sightsForMaxAngle * sensorFrequency
    maxVisibleAngle = if (ticksForSeeMaxAngel > sensorTickUntilAction) {
      floor(sensorTickUntilAction / sensorFrequency) * viewAngel
    } else {
      boundAngle * 2.0
    }
    return maxVisibleAngle
  }

  fun countSensorTickWithoutMainObject(): Int {
    return 0
  }

  fun estimateSensorTickWithoutMainObject(): Int {
    return 0
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