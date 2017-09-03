package teams.attack

import constants.*
import constants.server.SERVER_STOP_TIMEOUT
import control.Actions
import control.initPayer
import teams.Actors
import teams.PlayerConfig
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

open class AttackActors(private val teamName: String, private val configs: HashMap<String, PlayerConfig>) : Actors() {
  private fun mainActor(config: PlayerConfig): Runnable {
    return initPayer(teamName) { clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action = Actions(clientSocket, host, port)
      action.move(config.initialX, config.initialY)

      Thread.sleep(START_TIMEOUT)

      action.changeView(config.viewWidth, config.viewQuality)
      action.turnNeck(config.turnNeck)

      val scheduler = Executors.newScheduledThreadPool(1)
      scheduler.scheduleAtFixedRate({ action.receive() }, 0, 10, TimeUnit.MILLISECONDS)

      action.kick(config.kickPower, config.kickDirection)
      for (i in 0..TICK_MAX - 3) {
        action.turnNeck(if (i % 2 == 0) 80 else -80)
        action.dash(config.dashPower)
      }
      config.kickDirection = 50
      action.kick(config.kickPower, config.kickDirection)

      scheduler.shutdown()
      Thread.sleep(SERVER_STOP_TIMEOUT)
    }
  }

  private fun upperAttacker(config: PlayerConfig): Runnable {
    return sillyRotateActor(config, teamName)
  }

  private fun lowerAttacker(config: PlayerConfig): Runnable {
    return sillyRotateActor(config, teamName)
  }

  override fun getActorThreads(): List<Thread> {
    if (actors.isNotEmpty()) {
      actors.clear()
    }
    actors.add(Thread(mainActor(configs[KICKER]!!), KICKER))
    actors.add(Thread(upperAttacker(configs[UP_ATTACKER]!!), UP_ATTACKER))
    actors.add(Thread(lowerAttacker(configs[LOW_ATTACKER]!!), LOW_ATTACKER))

    return actors
  }
}