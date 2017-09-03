package teams.attack

import constants.KICKER
import constants.LOW_ATTACKER
import constants.UP_ATTACKER
import teams.PlayerConfig

class Attack(teamName: String = "Attack",
             configs: HashMap<String, PlayerConfig>
             = hashMapOf(
                 Pair(KICKER, PlayerConfig(-5, 0, -32, 20, 90, 90)),
                 Pair(UP_ATTACKER, PlayerConfig(4, -9)),
                 Pair(LOW_ATTACKER, PlayerConfig(3, 7))))
  : AttackActors(teamName, configs)