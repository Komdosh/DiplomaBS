package diploma.vision

import diploma.constants.server.TICK
import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth
import diploma.control.Action
import diploma.estimate.EstimateSubSystem
import diploma.estimate.EstimateSubSystemImpl
import diploma.model.VisiblePlayer
import diploma.teams.PlayerConfig
import java.net.DatagramPacket
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.floor

class VisualSensorAlgorithm(private val config: PlayerConfig, private val actorControl: Action) {

    private val estimateBound = 10
    private val boundAngle: Double = 90.0

    private var maxVisibleAngle: Double = 0.0
    private val lowQualityPlayersInfo: MutableMap<Int, List<VisiblePlayer>> = HashMap()
    private val highQualityPlayersInfo: MutableMap<Int, List<VisiblePlayer>> = HashMap()
    private val anglesToView: MutableList<Int> = ArrayList()
    private val estimateSubSystem: EstimateSubSystem = EstimateSubSystemImpl()
    private val tickService: TickService = TickServiceImpl()
    private val sensorTick = SensorTick(tickService)
    private val angleService: AngleService = AngleServiceImpl()
    private var currentViewWidthForLow: ViewWidth = ViewWidth.NORMAL

    fun start() {
        tickService.startTick()
        initView()
    }

    private fun initView() {
        calculateMaxVisibleAngle(currentViewWidthForLow, ViewQuality.LOW)
        sensorTick.calculateForGetMinimalQualityInfo(maxVisibleAngle, currentViewWidthForLow)
        sensorTick.calculateWithoutMainObject(currentViewWidthForLow, ViewQuality.LOW, maxVisibleAngle)
        sensorTick.estimateWithoutMainObject(currentViewWidthForLow, maxVisibleAngle)
        getVisualInfo(false)
    }

    private fun calculateMaxVisibleAngle(viewWidth: ViewWidth, viewQuality: ViewQuality): Double {
        val viewAngle = getViewAngle(viewWidth)
        val sightsCountForMaxAngle = getSightsCountForAngle(boundAngle, viewAngle)
        val sensorFrequency = getViewFrequency(viewWidth, viewQuality) / TICK
        val ticksForSeeMaxAngle = sightsCountForMaxAngle * sensorFrequency
        val sensorTickUntilAction = sensorTick.countUntilAction(viewWidth, viewQuality)
        maxVisibleAngle = if (ticksForSeeMaxAngle > sensorTickUntilAction) {
            floor(sensorTickUntilAction / sensorFrequency) * viewAngle
        } else {
            boundAngle * 2.0
        }
        return maxVisibleAngle
    }

    private fun getSightsCountForAngle(maxAngle: Double, viewAngle: Double) = ceil((maxAngle * 2) / viewAngle)

    private fun getNeckAngleAndIterationWMO(turnNeckOnce: Boolean, viewWidth: ViewWidth, neckAngle: Int, iterationWithoutMainObject: Int): Pair<Int, Int> {
        var localNeckAngle = neckAngle
        var localIterationWithoutMainObject = iterationWithoutMainObject
        if (!turnNeckOnce && sensorTick.estimateWithoutMainObject > 0) {
            if (!isObjectInAngle(viewWidth, angleService.getBodyObjectAngle(), localNeckAngle)) {
                if (sensorTick.estimateWithoutMainObject == localIterationWithoutMainObject) {
                    localNeckAngle = angleService.getBodyObjectAngle()
                    localIterationWithoutMainObject = 0
                } else {
                    ++localIterationWithoutMainObject
                }
            } else {
                localIterationWithoutMainObject = 0
            }
        }
        return Pair(localNeckAngle, localIterationWithoutMainObject)
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
        anglesToView.clear()
        var neckAngle = (-(maxVisibleAngle / 2) + (viewAngle / 2)).toInt()
        while (neckAngle < (maxVisibleAngle / 2)) {
            anglesToView.add(neckAngle)
            neckAngle += viewAngle.toInt()
        }
    }

    private fun getVisualInfo(isHigh: Boolean) {
        val viewWidth: ViewWidth
        val viewQuality: ViewQuality
        if (isHigh) {
            viewWidth = ViewWidth.NARROW
            viewQuality = ViewQuality.HIGH
        } else {
            viewWidth = currentViewWidthForLow
            viewQuality = ViewQuality.LOW
            initAnglesForLowQuality(getViewAngle(viewWidth))
        }
        changeView(viewWidth, viewQuality)

        val turnNeckOnce = anglesToView.size == 1

        val scheduleFrequency = getViewFrequency(viewWidth, viewQuality).toLong()
        var sensorTickCounter = 0
        var anglesCounter = 0
        var allAnglesChecked = false
        var iterationWithoutMainObject = 0
        schedulerByView(0, viewWidth, viewQuality) { scheduler ->
            var neckAngle = anglesToView[anglesCounter]

            val (angle, iteration) = getNeckAngleAndIterationWMO(turnNeckOnce, viewWidth, neckAngle, iterationWithoutMainObject)
            iterationWithoutMainObject = iteration
            neckAngle = angle

            if (neckAngle != anglesToView[anglesCounter]) {
                --anglesCounter
            }

            turnNeckAndGetInfo(neckAngle, scheduleFrequency, if (isHigh) highQualityPlayersInfo else lowQualityPlayersInfo)

            if (allAnglesChecked || (!isHigh && sensorTickCounter == sensorTick.forGettingMinimalQualityInfo)) {
                afterGetInfo(isHigh)
                scheduler.shutdown()
            }

            anglesCounter = (anglesCounter + 1) % anglesToView.size
            if (anglesCounter == 0) {
                allAnglesChecked = true
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

    private fun turnNeckAndGetInfo(angle: Int, scheduleFrequency: Long, qualityMap: MutableMap<Int, List<VisiblePlayer>>):
            List<VisiblePlayer> {
        var vp: List<VisiblePlayer> = ArrayList()

        var message = getServerMessage(turnNeck(angle))
        var isSee = isMessageSee(message)

        if ((isSee && vp.isEmpty())) {
            Thread.sleep(scheduleFrequency)
            message = getServerMessage(actorControl.receive())
            isSee = isMessageSee(message)
        }

        if (isSee) {
            vp = getVisiblePlayers(message)
        }

        if (vp.isNotEmpty()) {
            qualityMap[angle] = vp
        }
        return vp
    }

    private fun isObjectInAngle(viewWidth: ViewWidth, objectBodyAngle: Int, direction: Int): Boolean {
        val halfSeeAngle = getViewAngle(viewWidth) / 2
        return objectBodyAngle > (halfSeeAngle - direction) && objectBodyAngle < (halfSeeAngle + direction)
    }

    private fun afterGetInfo(isHigh: Boolean) {
        println(if (isHigh) highQualityPlayersInfo else lowQualityPlayersInfo)
        val mapForEstimate = if (isHigh) highQualityPlayersInfo else lowQualityPlayersInfo

        if (mapForEstimate.isEmpty()) {
            if (!isHigh) {
                currentViewWidthForLow = when (currentViewWidthForLow) {
                    ViewWidth.WIDE -> ViewWidth.NORMAL
                    ViewWidth.NORMAL -> ViewWidth.NARROW
                    ViewWidth.NARROW -> ViewWidth.NORMAL
                }
                initView()
            } else {
                getVisualInfo(false)
            }

            return
        }

        anglesToView.clear()
        var averageEstimate = 0.0
        mapForEstimate.forEach { (angle, visiblePlayers) ->
            averageEstimate = correctConfigWithEstimate(visiblePlayers, averageEstimate, angle)
            if (!isHigh && averageEstimate > estimateBound) {
                anglesToView.add(angle)
            }
        }

        getVisualInfo(!isHigh)
    }

    private fun correctConfigWithEstimate(visiblePlayers: List<VisiblePlayer>, estimateIn: Double, angle: Int): Double {
        var estimate = estimateIn
        val averageEstimate = visiblePlayers.map { estimateSubSystem.forVisiblePlayer(it) }.average()
        if (averageEstimate > estimate) {
            config.kickDirection = angle
            estimate = averageEstimate
        }
        println("Angle: $angle, Estimate: $averageEstimate")
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

    private fun isTimeOver(viewWidth: ViewWidth, viewQuality: ViewQuality): Boolean = sensorTick.countUntilAction(viewWidth, viewQuality) < 1

    private fun log(viewWidth: ViewWidth, viewQuality: ViewQuality) {
        println(this)
        println("Frequency for $viewWidth $viewQuality: ${getViewFrequency(viewWidth, viewQuality)}")
        println("Angle for $viewWidth: ${getViewAngle(viewWidth)}")
    }

    override fun toString(): String {
        return "VisualSensorAlgorithm(config=$config, actorControl=$actorControl, estimateBound=$estimateBound, boundAngle=$boundAngle, maxVisibleAngle=$maxVisibleAngle, sensorTick=$sensorTick, lowQualityPlayersInfo=$lowQualityPlayersInfo, highQualityPlayersInfo=$highQualityPlayersInfo, anglesToView=$anglesToView, estimateSubSystem=$estimateSubSystem, tickService=$tickService, angleService=$angleService, currentViewWidthForLow=$currentViewWidthForLow)"
    }
}
