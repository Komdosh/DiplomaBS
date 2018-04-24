package diploma.vision

import diploma.constants.server.TICK
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class TickServiceImpl : TickService {
  private var tickUntilAction: AtomicInteger = AtomicInteger(8) //hardcoded for now

  override fun getUntilAction(): Int {
    return tickUntilAction.get()
  }

  override fun serverTicked(): Int {
    return tickUntilAction.decrementAndGet()
  }

  override fun isTimeOver() = tickUntilAction.get() <= 1

  override fun startTick() {
    val scheduler = Executors.newScheduledThreadPool(1)
    scheduler.scheduleAtFixedRate({
      if (serverTicked() == 0) {
        scheduler.shutdown()
      }
    }, 0, TICK.toLong(), TimeUnit.MILLISECONDS)
  }

  override fun toString(): String {
    return "TickServiceImpl(tickUntilAction=$tickUntilAction)"
  }
}