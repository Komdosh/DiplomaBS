package teams

import constants.START_TIMEOUT
import constants.TICK_MAX
import control.Actions
import control.initPayer
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class Attack {
  val teamName: String = "Attack"

  fun mainActor(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move(-5, 0)

      Thread.sleep(START_TIMEOUT)
      action.changeView("narrow", "low")
      action.turnNeck(-45)

      val scheduler = Executors.newScheduledThreadPool(1)
      scheduler.scheduleAtFixedRate({ action.receive() }, 0, 10, TimeUnit.MILLISECONDS)

      action.kick(20, 0)
      for (i in 0..TICK_MAX - 3) {
        action.turnNeck(if (i % 2 == 0) 90 else -90)
        action.dash(90)
      }
      action.kick(20, 50)
      scheduler.shutdown()
      Thread.sleep(100000L)
    }
  }

  fun upperAttacker(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move(4, -9)

      action.runAndReturn()
      Thread.sleep(100000L)
    }
  }

  fun lowerAttacker(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move(3, 7)

      action.runAndReturn()
      Thread.sleep(100000L)
    }
  }
}