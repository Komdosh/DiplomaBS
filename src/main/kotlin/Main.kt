import constants.server.SERVER_START_TIMEOUT
import constants.server.SERVER_STOP_TIMEOUT
import constants.server.TICK
import teams.Actors
import teams.Trainings.Trainer
import teams.attack.Attack
import teams.def.Def
import java.lang.System.exit


fun main(args: Array<String>) {
  val def: Actors = Def()
  val attack: Actors = Attack()
  val trainer = Trainer()

  for (i in 0..3) {
    runServer(Runnable { runActors(attack, def, trainer) })
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
}

private fun runActors(attack: Actors, def: Actors, trainer: Trainer) {
  attack.getActorThreads().forEach({ it.start() })

  Thread.sleep(TICK)

  def.getActorThreads().forEach({ it.start() })

  Thread(trainer.init()).start()
}
