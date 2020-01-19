package diploma

import diploma.constants.KICKER
import diploma.constants.server.SERVER_START_TIMEOUT
import diploma.constants.server.SERVER_STOP_TIMEOUT
import diploma.constants.server.TICK
import diploma.teams.Actors
import diploma.teams.attack.Attack
import diploma.teams.def.Def
import diploma.teams.trainings.Trainer
import java.lang.System.exit
import kotlin.system.exitProcess

const val simulationRepeat: Int = 0


fun main() {

  val def = Def()
  val attack = Attack()
  val trainer = Trainer()

  for (i in 0..simulationRepeat) {
    runServer(Runnable { runActors(attack, def, trainer) })

    val turnNeckMoment = attack.configs[KICKER]?.turnNeck ?: error("No turn neck for kicker")
    val kickDirection = attack.configs[KICKER]?.kickDirection ?: error("No turn neck for kicker")
    println("turn neck moment: $turnNeckMoment | kick direction: $kickDirection")
    println("-----------------------------------------")
  }

  exitProcess(0)
}

private fun runServer(runOnServer: Runnable) {
/*  ProcessBuilder(
      "gnome-terminal",
      "-e",
      "rcsoccersim").start()

  Thread.sleep(SERVER_START_TIMEOUT)*/

  runOnServer.run()

/*  Thread.sleep(SERVER_STOP_TIMEOUT)

  ProcessBuilder(
      "gnome-terminal",
      "-e",
      "killall rcsoccersim").start()

  Thread.sleep(TICK.toLong())*/
}

private fun runActors(attack: Actors, def: Actors, trainer: Trainer) {
  attack.getActorThreads().forEach { it.start() }

  Thread.sleep(TICK.toLong())

  def.getActorThreads().forEach { it.start() }

  Thread(trainer.init()).start()
}
