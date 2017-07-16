import teams.Attack
import teams.Def

fun main(args: Array<String>) {
  val def : Def = Def()
  val attack : Attack = Attack()

  Thread(attack.mainActor()).start()

  Thread(attack.upperAttacker()).start()
  Thread(attack.lowerAttacker()).start()

  Thread(def.upperDef()).start()
  Thread(def.lowerDef()).start()
}
