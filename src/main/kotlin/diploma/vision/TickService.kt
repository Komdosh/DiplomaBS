package diploma.vision

interface TickService {
  fun getUntilAction(): Int
  fun serverTicked(): Int
  fun isTimeOver(): Boolean
  fun startTick()
}