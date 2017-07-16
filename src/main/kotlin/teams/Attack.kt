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
      action.move("35 0")

      for (i in 0..499) {
        Thread.sleep(200L)
      }
    }
  }

  fun upperAttacker(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move("40 -10")

      for (i in 0..499) {
        Thread.sleep(200L)
      }
    }
  }

  fun lowerAttacker(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move("40 10")

      for (i in 0..499) {
        Thread.sleep(200L)
      }
    }
  }
}