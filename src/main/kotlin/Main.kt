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

  Thread.sleep(2000)

  val streamOfPlayers: Stream<Runnable> = Stream.of(attack.mainActor(), attack.upperAttacker(), attack.lowerAttacker(),
      def.upperDef(), def.lowerDef())

  streamOfPlayers.forEach({
    Thread(it).start()
    Thread.sleep(1500L)
  })

  Thread(trainer.init()).start()

  Thread.sleep(50000)
  p.destroy()
}
