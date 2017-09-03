package teams.def

import constants.LOW_DEFER
import constants.UP_DEFER
import teams.Actors
import teams.PlayerConfig

open class DefActors(private val teamName: String, private val configs: HashMap<String, PlayerConfig>) : Actors() {

  private fun upperDef(config: PlayerConfig): Runnable {
    return sillyRotateActor(config, teamName)
  }

  private fun lowerDef(config: PlayerConfig): Runnable {
    return sillyRotateActor(config, teamName)
  }

  override fun getActorThreads(): List<Thread> {
    if (actors.isNotEmpty()) {
      actors.clear()
    }
    actors.add(Thread(upperDef(configs[UP_DEFER]!!), UP_DEFER))
    actors.add(Thread(lowerDef(configs[LOW_DEFER]!!), LOW_DEFER))
    return actors
  }
}