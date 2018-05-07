package diploma.teams.attack

import diploma.constants.KICKER
import diploma.constants.LOW_ATTACKER
import diploma.constants.UP_ATTACKER
import diploma.teams.PlayerConfig

class Attack(teamName: String = "Attack",
             val configs: HashMap<String, PlayerConfig>
             = hashMapOf(
                 Pair(KICKER, PlayerConfig(-13, 0, 0, 20, 0, 90)),
                 Pair(UP_ATTACKER, PlayerConfig(4, -3)), // 4 -3
                 Pair(LOW_ATTACKER, PlayerConfig(3, 5)))) // 3 5
  : AttackActors(teamName, configs)