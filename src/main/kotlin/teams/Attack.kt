package teams

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
      action.move("0 0")

      Thread.sleep(100000L)
    }
  }

  fun upperAttacker(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move("-5 -5")

      Thread.sleep(100000L)
    }
  }

  fun lowerAttacker(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move("-5 5")

      Thread.sleep(100000L)
    }
  }
}