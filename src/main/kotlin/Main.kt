import constants.MAIN_ACTOR
import constants.SERVER_START_TIMEOUT
import constants.SERVER_STOP_TIMEOUT
import teams.Attack
import teams.Def
import teams.Trainings.Trainer
import java.lang.System.exit
import java.util.stream.Stream


fun main(args: Array<String>) {
  val def: Def = Def()
  val attack: Attack = Attack()
  val trainer: Trainer = Trainer()

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

private fun runActors(attack: Attack, def: Def, trainer: Trainer) {
  val attackers: Stream<Runnable> = Stream.of(attack.upperAttacker(), attack.lowerAttacker())

  val defers: Stream<Runnable> = Stream.of(def.upperDef(), def.lowerDef())

  val mainActor = Thread(attack.mainActor())
  mainActor.name = MAIN_ACTOR
  mainActor.start()

  attackers.forEach({
    Thread(it).start()
  })

  defers.forEach({
    Thread(it).start()
  })

  Thread(trainer.init()).start()
}
