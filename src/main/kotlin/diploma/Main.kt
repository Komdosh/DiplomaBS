package diploma

import diploma.constants.KICKER
import diploma.constants.server.SERVER_START_TIMEOUT
import diploma.constants.server.SERVER_STOP_TIMEOUT
import diploma.constants.server.TICK
import diploma.teams.Actors
import diploma.teams.attack.Attack
import diploma.teams.def.Def
import teams.Trainings.Trainer
import java.lang.System.exit

const val simulationRepeat: Int = 0


fun main(args: Array<String>) {

  val def = Def()
  val attack = Attack()
  val trainer = Trainer()

  for (i in 0..simulationRepeat) {
    runServer(Runnable { runActors(attack, def, trainer) })

    val turnMoment = attack.configs[KICKER]!!.turnNeck
    val kickDirection = attack.configs[KICKER]!!.kickDirection
    println("turn moment: $turnMoment | kick direction: $kickDirection")
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

  Thread.sleep(TICK.toLong())
}

private fun runActors(attack: Actors, def: Actors, trainer: Trainer) {
  attack.getActorThreads().forEach({ it.start() })

  Thread.sleep(TICK.toLong())

  def.getActorThreads().forEach({ it.start() })

  Thread(trainer.init()).start()
}
