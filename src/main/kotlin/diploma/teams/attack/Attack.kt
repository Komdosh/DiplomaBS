package diploma.teams.attack

import diploma.constants.KICKER
import diploma.constants.LOW_ATTACKER
import diploma.constants.UP_ATTACKER
import diploma.teams.PlayerConfig

class Attack(teamName: String = "Attack",
             val configs: HashMap<String, PlayerConfig>
             = hashMapOf(
                 Pair(KICKER, PlayerConfig(-8, 0, 0, 20, 90, 90)),
                 Pair(UP_ATTACKER, PlayerConfig(4, -9)),
                 Pair(LOW_ATTACKER, PlayerConfig(3, 7))))
  : AttackActors(teamName, configs)