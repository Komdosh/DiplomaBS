package teams

import control.Actions
import control.initPayer
import java.net.DatagramSocket
import java.net.InetAddress

class Def {
  val teamName: String = "Def"

  fun upperDef(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move("-5 -5")

      Thread.sleep(100000L)
    }
  }

  fun lowerDef(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move("-5 5")

      Thread.sleep(100000L)
    }
  }
}
