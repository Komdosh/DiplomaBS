package diploma.teams.def

import diploma.constants.LOW_DEFER
import diploma.constants.UP_DEFER
import diploma.teams.PlayerConfig

class Def(teamName: String = "Def",
          configs: HashMap<String, PlayerConfig>
          = hashMapOf(
              Pair(UP_DEFER, PlayerConfig(-3, -8)),
              Pair(LOW_DEFER, PlayerConfig(-3, 8))))
  : DefActors(teamName, configs)
