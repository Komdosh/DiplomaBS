package diploma.teams.attack

import diploma.constants.*
import diploma.constants.server.SERVER_STOP_TIMEOUT
import diploma.control.Actions
import diploma.control.initPayer
import diploma.teams.Actors
import diploma.teams.PlayerConfig
import diploma.visiblePlayers
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
      action.turnNeck(config.initialTurnNeck)

      val scheduler = Executors.newScheduledThreadPool(1)
      scheduler.scheduleAtFixedRate({ action.receive() }, 0, 10, TimeUnit.MILLISECONDS)

      action.kick(config.kickPower, config.kickDirection)
      var j = 0
      for (i in 0..TICK_MAX - 3) {
        action.turnNeck(if (i % 2 == 0) config.turnNeck else -config.initialTurnNeck)
        action.dash(config.dashPower)
      }
      val catcher = visiblePlayers.findLast { it.teamName == teamName }
      //println("catcher: $catcher")
      config.kickDirection = catcher?.direction ?: config.kickDirection
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