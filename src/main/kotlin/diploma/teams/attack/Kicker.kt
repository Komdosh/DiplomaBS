package diploma.teams.attack

import diploma.constants.START_TIMEOUT
import diploma.constants.TICK_MAX
import diploma.constants.server.SERVER_STOP_TIMEOUT
import diploma.control.Action
import diploma.teams.PlayerConfig
import diploma.vision.VisualSensorAlgorithm
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

open class Kicker(private val config: PlayerConfig) {


  fun getAlgorithm(): (DatagramSocket, InetAddress, Int) -> Unit {
    return { clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action = Action(clientSocket, host, port)
      action.move(config.initialX, config.initialY)
      val visualSensorAlgorithm = VisualSensorAlgorithm(action)

      Thread.sleep(START_TIMEOUT)

      action.changeView(config.viewWidth, config.viewQuality)
      action.turnNeck(config.initialTurnNeck)

      val scheduler = Executors.newScheduledThreadPool(1)
      scheduler.scheduleAtFixedRate({ action.receive() }, 0, 10, TimeUnit.MILLISECONDS)

      action.kick(config.kickPower, config.kickDirection)

      visualSensorAlgorithm.start()
      for (i in 0..TICK_MAX - 3) {
        action.dash(config.dashPower)
      }

      action.kick(config.kickPower, config.kickDirection)

      scheduler.shutdown()
      Thread.sleep(SERVER_STOP_TIMEOUT)
    }
  }
}