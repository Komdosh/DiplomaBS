package diploma.vision

interface TickService {
  fun getUntilAction(): Int
  fun isTimeOver(): Boolean
  fun startTick()
}
