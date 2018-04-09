package diploma

import diploma.constants.KICKER
import diploma.constants.server.SERVER_START_TIMEOUT
import diploma.constants.server.SERVER_STOP_TIMEOUT
import diploma.constants.server.TICK
import diploma.model.VisiblePlayer
import diploma.teams.Actors
import diploma.teams.attack.Attack
import diploma.teams.def.Def
import teams.Trainings.Trainer
import java.lang.System.exit

var visiblePlayersCount = 0
val visiblePlayers: MutableList<VisiblePlayer> = ArrayList()
const val simulationRepeat: Int = 1


fun main(args: Array<String>) {
  val def = Def()
  val attack = Attack()
  val trainer = Trainer()

  for (i in 0..simulationRepeat) {
    runServer(Runnable { runActors(attack, def, trainer) })

    val turnMoment = attack.configs[KICKER]!!.turnNeck
    val kickDirection = attack.configs[KICKER]!!.kickDirection
    println("turn moment: $turnMoment | kick direction: $kickDirection | seeing players: ${visiblePlayersCount}")

    visiblePlayersCount = 0
    visiblePlayers.clear()

    if (turnMoment - 10 == 10) {
      attack.configs[KICKER]!!.turnNeck = 90
    } else {
      attack.configs[KICKER]!!.turnNeck = turnMoment - 10
    }
    println("-----------------------------------------")
  }

  exit(0)
}

private fun runServer(runOnServer: Runnable) {
  ProcessBuilder(
      "gnome-terminal",
      "-e",
      "rcsoccersim").start()

  Thread.sleep(SERVER_START_TIMEOUT)

  runOnServer.run()

  Thread.sleep(SERVER_STOP_TIMEOUT)

  ProcessBuilder(
      "gnome-terminal",
      "-e",
      "killall rcsoccersim").start()

  Thread.sleep(TICK)
}

private fun runActors(attack: Actors, def: Actors, trainer: Trainer) {
  attack.getActorThreads().forEach({ it.start() })

  Thread.sleep(TICK)

  def.getActorThreads().forEach({ it.start() })

  Thread(trainer.init()).start()
}
