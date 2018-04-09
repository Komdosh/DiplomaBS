package diploma.vision

class VisualSensorAlgorithm {

  private var tickUntilAction: Int = 9 //hardcoded for now
  private var sensorTickUntilAction: Int = 0
  private var maxVisibleAngle: Int = 0
  private var sensorTickWithoutMainObject: Int = 0
  private var estimateSensorTickWithoutMainObject: Int = 0
  private var visualTickForEstimate: Int = 0
  private var estimateUsefulnessAngles: Int = 0

  constructor() {

  }

  fun countTickUntilAction(): Int {
    return tickUntilAction
  }

  fun countSensorTickUntilAction(): Int {
    return 0
  }

  fun calculateMaxVisibleAngle(): Int {
    return 0
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