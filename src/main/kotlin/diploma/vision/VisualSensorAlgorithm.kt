package diploma.vision

import diploma.constants.server.TICK
import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth
import diploma.control.Action
import diploma.estimate.EstimateSubSystem
import diploma.estimate.EstimateSubSystemImpl
import diploma.model.VisiblePlayer
import diploma.teams.PlayerConfig
import java.lang.Math.ceil
import java.lang.Math.floor
import java.net.DatagramPacket
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class VisualSensorAlgorithm(private val config: PlayerConfig, private val actorControl: Action) {

  private val estimateBound = 10
  private val boundAngel: Double = 90.0


  private var maxVisibleAngle: Double = 0.0
  private var sensorTickWithoutMainObject: Int = 0
  private var sensorTicksForGettingMinimalQualityInfo: Int = 0
  private var estimateSensorTickWithoutMainObject: Int = 0
  private val lowQualityPlayersInfo: MutableMap<Int, List<VisiblePlayer>> = HashMap()
  private val highQualityPlayersInfo: MutableMap<Int, List<VisiblePlayer>> = HashMap()
  private val angelsToView: MutableList<Int> = ArrayList()
  private val estimateSubSystem: EstimateSubSystem = EstimateSubSystemImpl()
  private val tickService: TickService = TickServiceImpl()
  private val angelService: AngelService = AngelServiceImpl()

  fun start() {
    tickService.startTick()
    countSensorTickUntilAction(ViewWidth.NARROW, ViewQuality.LOW)
    calculateMaxVisibleAngle(ViewWidth.NARROW, ViewQuality.LOW)
    calculateSensorTicksForGetMinimalQualityInfo()
    calculateSensorTickWithoutMainObject(ViewWidth.NARROW, ViewQuality.LOW)
    estimateSensorTickWithoutMainObject(ViewWidth.NARROW)
    getVisualInfo(false)
  }

  fun countSensorTickUntilAction(viewWidth: ViewWidth, viewQuality: ViewQuality): Int =
      floor(tickService.getUntilAction() * TICK / getViewFrequency(viewWidth, viewQuality)).toInt()

  fun calculateSensorTicksForGetMinimalQualityInfo(): Int {
    sensorTicksForGettingMinimalQualityInfo = ceil(maxVisibleAngle / getViewAngle(ViewWidth.NARROW) *
        getViewFrequency(ViewWidth.NARROW, ViewQuality.LOW) / TICK).toInt() + 1
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

  private fun getNeckAngelAndIterationWMO(turnNeckOnce: Boolean, viewWidth: ViewWidth, neckAngel: Int, iterationWithoutMainObject: Int): Pair<Int, Int> {
    var localNeckAngel = neckAngel
    var localIterationWithoutMainObject = iterationWithoutMainObject
    if (!turnNeckOnce && estimateSensorTickWithoutMainObject > 0) {
      if (!isObjectInAngel(viewWidth, angelService.getBodyObjectAngel(), localNeckAngel)) {
        if (estimateSensorTickWithoutMainObject == localIterationWithoutMainObject) {
          localNeckAngel = angelService.getBodyObjectAngel()
          localIterationWithoutMainObject = 0
        } else {
          ++localIterationWithoutMainObject
        }
      } else {
        localIterationWithoutMainObject = 0
      }
    }
    return Pair(localNeckAngel, localIterationWithoutMainObject)
  }

  private fun getVisiblePlayers(serverMessage: String): List<VisiblePlayer> {
    var vp: List<VisiblePlayer> = ArrayList()
    if (!serverMessage.contains("warning", true) && !serverMessage.contains("error", true)) {
      //println("FROM SERVER: $serverMessage")
      if (serverMessage.contains("(p \"", true)) {
        vp = parseVisiblePlayers(serverMessage)
        //println(vp)
      }
    }
    return vp
  }

  private fun initAnglesForLowQuality(viewAngle: Double = 90.0) {
    angelsToView.clear()
    var neckAngel = (-(maxVisibleAngle / 2) + (viewAngle / 2)).toInt()
    while (neckAngel < (maxVisibleAngle / 2)) {
      angelsToView.add(neckAngel)
      neckAngel += viewAngle.toInt()
    }
  }


  private fun getVisualInfo(isHigh: Boolean) {
    val viewWidth: ViewWidth
    val viewQuality: ViewQuality
    if (isHigh) {
      viewWidth = ViewWidth.NARROW
      viewQuality = ViewQuality.HIGH
    } else {
      viewWidth = ViewWidth.NARROW
      viewQuality = ViewQuality.LOW
      initAnglesForLowQuality(getViewAngle(viewWidth))
    }
    changeView(viewWidth, viewQuality)

    val turnNeckOnce = angelsToView.size == 1
    if (turnNeckOnce) {
      turnNeck(angelsToView[0])
      afterGetInfo(isHigh)
      return
    }

    val scheduleFrequency = getViewFrequency(viewWidth, viewQuality).toLong()
    var sensorTickCounter = 0
    var angelsCounter = 0
    var allAngelsChecked = false
    var iterationWithoutMainObject = 0
    schedulerByView(0, viewWidth, viewQuality) { scheduler ->
      var neckAngel = angelsToView[angelsCounter]

      val (angel, iteration) = getNeckAngelAndIterationWMO(turnNeckOnce, viewWidth, neckAngel, iterationWithoutMainObject)
      iterationWithoutMainObject = iteration
      neckAngel = angel

      if (neckAngel != angelsToView[angelsCounter]) {
        --angelsCounter
      }

      turnNeckAndGetInfo(turnNeckOnce, neckAngel, scheduleFrequency, highQualityPlayersInfo)

      if (allAngelsChecked || (!isHigh && sensorTickCounter == sensorTicksForGettingMinimalQualityInfo)) {
        afterGetInfo(isHigh)
        scheduler.shutdown()
      }

      angelsCounter = (angelsCounter + 1) % angelsToView.size
      if (angelsCounter == 0) {
        allAngelsChecked = true
      }
      ++sensorTickCounter
    }
  }

  private fun turnNeck(turnNeck: Int): DatagramPacket {
    config.turnNeck = turnNeck
    return actorControl.turnNeck(turnNeck)
  }

  private fun changeView(viewWidth: ViewWidth, viewQuality: ViewQuality) {
    actorControl.changeView(viewWidth, viewQuality)
    config.viewWidth = viewWidth
    config.viewQuality = viewQuality
  }

  private fun turnNeckAndGetInfo(turnNeckOnce: Boolean, angel: Int, scheduleFrequency: Long,
                                 qualityMap: MutableMap<Int, List<VisiblePlayer>>): List<VisiblePlayer> {
    var vp: List<VisiblePlayer> = ArrayList()
    var isSee = false
    var message = ""
    if (!turnNeckOnce) {
      message = getServerMessage(turnNeck(angel))
      isSee = isMessageSee(message)
    }

    if ((isSee && vp.isEmpty()) || turnNeckOnce) {
      if (!turnNeckOnce) {
        Thread.sleep(scheduleFrequency)
      }
      message = getServerMessage(actorControl.receive())
      isSee = isMessageSee(message)
    }

    if (isSee) {
      vp = getVisiblePlayers(message)
    }

    if (vp.isNotEmpty()) {
      qualityMap[angel] = vp
    }
    return vp
  }

  private fun isObjectInAngel(viewWidth: ViewWidth, objectBodyAngel: Int, direction: Int): Boolean {
    val halfSeeAngel = getViewAngle(viewWidth) / 2
    return objectBodyAngel > (halfSeeAngel - direction) && objectBodyAngel < (halfSeeAngel + direction)
  }

  private fun afterGetInfo(isHigh: Boolean) {
    println(if (isHigh) highQualityPlayersInfo else lowQualityPlayersInfo)
    val mapForEstimate = if (isHigh) lowQualityPlayersInfo else highQualityPlayersInfo

    if (mapForEstimate.isEmpty()) {
      getVisualInfo(false)
      return
    }

    angelsToView.clear()
    var averageEstimate = 0.0
    mapForEstimate.forEach { angel, visiblePlayers ->
      averageEstimate = correctConfigWithEstimate(visiblePlayers, averageEstimate, angel)
      if (!isHigh && averageEstimate > estimateBound) {
        angelsToView.add(angel)
      }
    }

    getVisualInfo(!isHigh)
  }

  private fun correctConfigWithEstimate(visiblePlayers: List<VisiblePlayer>, estimateIn: Double, angel: Int): Double {
    var estimate = estimateIn
    val averageEstimate = visiblePlayers.map { estimateSubSystem.forVisiblePlayer(it) }.average()
    if (averageEstimate > estimate) {
      config.kickDirection = angel
      estimate = averageEstimate
    }
    println("Angel: $angel, Estimate: $averageEstimate")
    return estimate
  }

  private fun schedulerByView(initialDelay: Long, viewWidth: ViewWidth, viewQuality: ViewQuality, run: (scheduler: ScheduledExecutorService) -> Unit) {
    if (isTimeOver(viewWidth, viewQuality)) {
      return
    }
    log(viewWidth, viewQuality)
    val scheduler = Executors.newScheduledThreadPool(1)
    val scheduleFreq = getViewFrequency(viewWidth, viewQuality).toLong()
    scheduler.scheduleAtFixedRate({
      run(scheduler)
      if (isTimeOver(viewWidth, viewQuality)) {
        scheduler.shutdown()
      }
    }, initialDelay, scheduleFreq, TimeUnit.MILLISECONDS)
  }

  private fun isTimeOver(viewWidth: ViewWidth, viewQuality: ViewQuality): Boolean = countSensorTickUntilAction(viewWidth, viewQuality) < 1

  private fun log(viewWidth: ViewWidth, viewQuality: ViewQuality) {
    println(this)
    println("Frequency for $viewWidth $viewQuality: ${getViewFrequency(viewWidth, viewQuality)}")
    println("Angel for $viewWidth: ${getViewAngle(viewWidth)}")
  }

  override fun toString(): String {
    return "VisualSensorAlgorithm(config=$config, estimateBound=$estimateBound, boundAngel=$boundAngel, maxVisibleAngle=$maxVisibleAngle, sensorTickWithoutMainObject=$sensorTickWithoutMainObject, sensorTicksForGettingMinimalQualityInfo=$sensorTicksForGettingMinimalQualityInfo, estimateSensorTickWithoutMainObject=$estimateSensorTickWithoutMainObject, lowQualityPlayersInfo=$lowQualityPlayersInfo, highQualityPlayersInfo=$highQualityPlayersInfo, angelsToView=$angelsToView, tickService=$tickService"
  }
}