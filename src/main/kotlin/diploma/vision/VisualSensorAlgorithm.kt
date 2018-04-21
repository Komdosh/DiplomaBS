package diploma.vision

import diploma.constants.server.TICK
import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth
import diploma.control.Action
import diploma.model.VisiblePlayer
import diploma.teams.PlayerConfig
import java.lang.Math.ceil
import java.lang.Math.floor
import java.net.DatagramPacket
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class VisualSensorAlgorithm(private val config: PlayerConfig, private val actorControl: Action) {

  private val estimateBound = 10
  private val boundAngel: Double = 90.0

  private var tickUntilAction: AtomicInteger = AtomicInteger(8) //hardcoded for now
  private var maxVisibleAngle: Double = 0.0
  private var sensorTickWithoutMainObject: Int = 0
  private var sensorTicksForGettingMinimalQualityInfo: Int = 0
  private var estimateSensorTickWithoutMainObject: Int = 0
  private val lowQualityPlayersInfo: MutableMap<Int, List<VisiblePlayer>> = HashMap()
  private val highQualityPlayersInfo: MutableMap<Int, List<VisiblePlayer>> = HashMap()
  private val usefulAngels: MutableList<Int> = ArrayList()

  fun start() {
    val scheduler = Executors.newScheduledThreadPool(1)
    scheduler.scheduleAtFixedRate({
      if (serverTicked() == 0) {
        scheduler.shutdown()
      }
    }, 0, TICK.toLong(), TimeUnit.MILLISECONDS)

    countSensorTickUntilAction(ViewWidth.NARROW, ViewQuality.LOW)
    calculateMaxVisibleAngle(ViewWidth.NARROW, ViewQuality.LOW)
    calculateSensorTicksForGetMinimalQualityInfo()
    calculateSensorTickWithoutMainObject(ViewWidth.NARROW, ViewQuality.LOW)
    estimateSensorTickWithoutMainObject(ViewWidth.NARROW)
    getLowQualityVisualInfo()
  }

  fun getTicksUntilAction(): Int {
    return tickUntilAction.get()
  }

  fun serverTicked(): Int = tickUntilAction.decrementAndGet()

  fun countSensorTickUntilAction(viewWidth: ViewWidth, viewQuality: ViewQuality): Int =
      floor(getTicksUntilAction() * TICK / getViewFrequency(viewWidth, viewQuality)).toInt()

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

  fun getLowQualityVisualInfo(viewWidth: ViewWidth = ViewWidth.NARROW, viewQuality: ViewQuality = ViewQuality.LOW) {
    actorControl.changeView(viewWidth, viewQuality)
    var sensorTickCounter = 0
    val viewAngle = getViewAngle(viewWidth)
    var neckAngel = (-boundAngel + (viewAngle / 2)).toInt()

    schedulerByView(viewWidth, viewQuality) { scheduler ->
      actorControl.turnNeck(neckAngel)
      val vp: List<VisiblePlayer> = getVisiblePlayers(actorControl.receive())
      if (vp.isNotEmpty()) {
        lowQualityPlayersInfo[neckAngel] = vp
      }
      neckAngel += viewAngle.toInt()
      if (sensorTickCounter == sensorTicksForGettingMinimalQualityInfo + 1) {
        afterLowQualityGet()
        scheduler.shutdown()
      }
      ++sensorTickCounter
    }
  }

  private fun getVisiblePlayers(receivePacket: DatagramPacket): List<VisiblePlayer> {
    val modifiedSentence = String(receivePacket.data, 0, receivePacket.length - 1)

    var vp: List<VisiblePlayer> = ArrayList()
    if (!modifiedSentence.contains("warning", true) && !modifiedSentence.contains("error", true)) {
      //println("FROM SERVER: $modifiedSentence")
      if (modifiedSentence.contains("(p \"", true)) {
        vp = parseVisiblePlayers(modifiedSentence)
        //println(vp)
      }
    }
    return vp
  }

  private fun afterLowQualityGet() {
    println(lowQualityPlayersInfo)
    if (lowQualityPlayersInfo.isEmpty()) {
      getLowQualityVisualInfo()
    } else {
      calculateEstimateUsefulnessAngles()
      getHighQualityVisualInfo()
    }
  }

  fun calculateEstimateUsefulnessAngles() {
    lowQualityPlayersInfo.forEach { angel, visiblePlayers ->
      val averageEstimate = visiblePlayers.map { getEstimateForVisiblePlayer(it) }.average()
      println("Angel: $angel, Estimate: $averageEstimate")
      if (averageEstimate > estimateBound) {
        usefulAngels.add(angel)
      }
    }
  }

  private fun getEstimateForVisiblePlayer(vp: VisiblePlayer): Int {
    val attackTeamName = "Attack"
    var estimate = 0
    estimate += if (vp.teamName.isNullOrBlank()) 0 else 2
    estimate += if (vp.teamName.equals(attackTeamName)) 10 else 5
    estimate += if (vp.direction == null) 0 else 6
    estimate += if (vp.ext == null) 0 else 4
    estimate += if (vp.distance == null) 0 else 6
    return estimate
  }

  private fun getHighQualityVisualInfo(viewWidth: ViewWidth = ViewWidth.NARROW, viewQuality: ViewQuality = ViewQuality.HIGH) {
    actorControl.changeView(viewWidth, viewQuality)
    var angelsCounter = 0

    schedulerByView(viewWidth, viewQuality) { scheduler ->
      actorControl.turnNeck(usefulAngels[angelsCounter])
      val vp: List<VisiblePlayer> = getVisiblePlayers(actorControl.receive())
      if (vp.isNotEmpty()) {
        highQualityPlayersInfo[usefulAngels[angelsCounter]] = vp
      }
      ++angelsCounter
      if (usefulAngels.size == angelsCounter) {
        afterHighQualityGet()
        scheduler.shutdown()
      }
    }
  }

  private fun afterHighQualityGet() {
    println(highQualityPlayersInfo)
    var estimate = 0.0
    val mapForEstimate = if (highQualityPlayersInfo.isEmpty()) lowQualityPlayersInfo else highQualityPlayersInfo
    mapForEstimate.forEach { angel, visiblePlayers ->
      val averageEstimate = visiblePlayers.map { getEstimateForVisiblePlayer(it) }.average()
      if (averageEstimate > estimate) {
        config.kickDirection = angel
        estimate = averageEstimate
      }
    }
  }

  private fun schedulerByView(viewWidth: ViewWidth, viewQuality: ViewQuality, run: (scheduler: ScheduledExecutorService) -> Unit) {
    log(viewWidth, viewQuality)
    val scheduler = Executors.newScheduledThreadPool(1)
    val scheduleFreq = getViewFrequency(viewWidth, viewQuality).toLong()
    scheduler.scheduleAtFixedRate({ run(scheduler) }, 0, scheduleFreq, TimeUnit.MILLISECONDS)
  }

  private fun log(viewWidth: ViewWidth, viewQuality: ViewQuality) {
    println(this)
    println("Frequency for $viewWidth $viewQuality: ${getViewFrequency(viewWidth, viewQuality)}")
    println("Angel for $viewWidth: ${getViewAngle(viewWidth)}")
  }

  override fun toString(): String {
    return "VisualSensorAlgorithm(boundAngel=$boundAngel, tickUntilAction=$tickUntilAction, maxVisibleAngle=$maxVisibleAngle, sensorTickWithoutMainObject=$sensorTickWithoutMainObject, sensorTicksForGettingMinimalQualityInfo=$sensorTicksForGettingMinimalQualityInfo, estimateSensorTickWithoutMainObject=$estimateSensorTickWithoutMainObject, lowQualityPlayersInfo=$lowQualityPlayersInfo)"
  }
}