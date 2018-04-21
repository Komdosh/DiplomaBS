package diploma.teams.attack

import diploma.constants.KICKER
import diploma.constants.LOW_ATTACKER
import diploma.constants.UP_ATTACKER
import diploma.control.initPayer
import diploma.teams.Actors
import diploma.teams.PlayerConfig

open class AttackActors(private val teamName: String, private val configs: HashMap<String, PlayerConfig>) : Actors() {
  private fun kicker(config: PlayerConfig): Runnable {
    val kicker = Kicker(config)
    return initPayer(teamName, kicker.getAlgorithm())
  }

  private fun upperAttacker(config: PlayerConfig): Runnable {
    return sillyRotateActor(config, teamName)
  }

  private fun lowerAttacker(config: PlayerConfig): Runnable {
    return sillyRotateActor(config, teamName)
  }

  override fun getActorThreads(): List<Thread> {
    if (actors.isNotEmpty()) {
      actors.clear()
    }
    actors.add(Thread(kicker(configs[KICKER]!!), KICKER))
    actors.add(Thread(upperAttacker(configs[UP_ATTACKER]!!), UP_ATTACKER))
    actors.add(Thread(lowerAttacker(configs[LOW_ATTACKER]!!), LOW_ATTACKER))

    return actors
  }
}