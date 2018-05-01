package diploma.teams.def

import diploma.constants.LOW_DEFER
import diploma.constants.SPY
import diploma.constants.UP_DEFER
import diploma.teams.PlayerConfig

class Def(teamName: String = "Def",
          configs: HashMap<String, PlayerConfig>
          = hashMapOf(
              Pair(SPY, PlayerConfig(10, 0)),
              Pair(UP_DEFER, PlayerConfig(-3, 6)), //-3 6
              Pair(LOW_DEFER, PlayerConfig(-3, -6)))) //-3 -6
  : DefActors(teamName, configs)
