import constants.TICK
import teams.Attack
import teams.Def
import teams.Trainings.Trainer
import java.util.stream.Stream


fun main(args: Array<String>) {

  val def : Def = Def()
  val attack : Attack = Attack()
  val trainer: Trainer = Trainer()

  val p = ProcessBuilder(
      "gnome-terminal",
      "-e",
      "rcsoccersim").start()

  Thread.sleep(1500)

  val attackers: Stream<Runnable> = Stream.of(
      attack.mainActor(), attack.upperAttacker(), attack.lowerAttacker())

  val defers: Stream<Runnable> = Stream.of(
      def.upperDef(), def.lowerDef())

  attackers.forEach({
    Thread(it).start()
  })

  Thread.sleep(TICK)

  defers.forEach({
    Thread(it).start()
  })

  Thread(trainer.init()).start()

  Thread.sleep(50000)
  p.destroy()
}
