import constants.KICKER
import constants.server.SERVER_START_TIMEOUT
import constants.server.SERVER_STOP_TIMEOUT
import constants.server.TICK
import model.VisiblePlayer
import teams.Actors
import teams.Trainings.Trainer
import teams.attack.Attack
import teams.def.Def
import java.lang.System.exit

var visiblePlayersCount = 0
val visiblePlayers: MutableList<VisiblePlayer> = ArrayList()

fun main(args: Array<String>) {
  val def = Def()
  val attack = Attack()
  val trainer = Trainer()

  for (i in 0..60) {
    runServer(Runnable { runActors(attack, def, trainer) })

    val turnMoment = attack.configs[KICKER]!!.turnNeck
    val kickDirection = attack.configs[KICKER]!!.kickDirection
    println("turn moment: $turnMoment | kick direction: $kickDirection | seeing players: $visiblePlayersCount")

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
