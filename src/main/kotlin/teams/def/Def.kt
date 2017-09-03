package teams.def

import constants.LOW_DEFER
import constants.UP_DEFER
import teams.PlayerConfig

class Def(teamName: String = "Def",
          configs: HashMap<String, PlayerConfig>
          = hashMapOf(
              Pair(UP_DEFER, PlayerConfig(-3, -8)),
              Pair(LOW_DEFER, PlayerConfig(-3, 8))))
  : DefActors(teamName, configs)
