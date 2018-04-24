package diploma.estimate

import diploma.model.VisiblePlayer

class EstimateSubSytemImpl : EstimateSubSystem {
  override fun forVisiblePlayer(vp: VisiblePlayer): Double {
    val attackTeamName = "Attack"
    var estimate = 0.0
    estimate += if (vp.teamName.isNullOrBlank()) 0 else 2
    estimate += if (vp.teamName.equals(attackTeamName)) 10 else 5
    estimate += if (vp.direction == null) 0 else 6
    estimate += if (vp.ext == null) 0 else 4
    estimate += if (vp.distance == null) 0 else 6
    return estimate
  }
}