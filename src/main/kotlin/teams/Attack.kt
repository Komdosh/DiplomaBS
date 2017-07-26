package teams

import constants.START_TIMEOUT
import constants.TICK_MAX
import control.Actions
import control.initPayer
import java.net.DatagramSocket
import java.net.InetAddress

class Attack {
  val teamName: String = "Attack"

  fun mainActor(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move(-5, 0)

      Thread.sleep(START_TIMEOUT)
      action.kick(20, 0)
      for (i in 0..TICK_MAX - 3) {
        action.dash(90)
      }
      action.kick(20, 50)
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