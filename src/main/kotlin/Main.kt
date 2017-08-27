import constants.MAIN_ACTOR
import constants.TICK
import teams.Attack
import teams.Def
import teams.Trainings.Trainer
import java.util.stream.Stream


fun main(args: Array<String>) {
  runGame()
}

private fun runGame() {
  val def: Def = Def()
  val attack: Attack = Attack()
  val trainer: Trainer = Trainer()

  val p = ProcessBuilder(
      "gnome-terminal",
      "-e",
      "rcsoccersim").start()

  Thread.sleep(1500)

  val attackers: Stream<Runnable> = Stream.of(attack.upperAttacker(), attack.lowerAttacker())

  val defers: Stream<Runnable> = Stream.of(def.upperDef(), def.lowerDef())

  val mainActor = Thread(attack.mainActor())
  mainActor.name = MAIN_ACTOR
  mainActor.start()

  attackers.forEach({
    Thread(it).start()
  })

  Thread.sleep(TICK)

  defers.forEach({
    Thread(it).start()
  })

  Thread(trainer.init()).start()
}
