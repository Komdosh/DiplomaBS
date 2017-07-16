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
      action.move("40 -10")

      for(i in 0..499) {
        Thread.sleep(200L)
      }
    }
  }

  fun lowerDef(): Runnable {
    return initPayer(teamName) {
      clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action: Actions = Actions(clientSocket, host, port)
      action.move("40 10")

      for(i in 0..499) {
        Thread.sleep(200L)
      }
    }
  }
}
