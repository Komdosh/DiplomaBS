package diploma.estimate

import diploma.model.VisiblePlayer

interface EstimateSubSystem {
  fun forVisiblePlayer(vp: VisiblePlayer): Double
}