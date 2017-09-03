package teams

import constants.server.SERVER_STOP_TIMEOUT
import control.Actions
import control.initPayer
import java.net.DatagramSocket
import java.net.InetAddress

abstract class Actors {
  protected val actors: MutableList<Thread> = ArrayList()

  abstract fun getActorThreads(): List<Thread>

  fun sillyRotateActor(config: PlayerConfig, teamName: String): Runnable {
    return initPayer(teamName) { clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action = Actions(clientSocket, host, port)
      action.move(config.initialX, config.initialY)

      action.runAndReturn()
      Thread.sleep(SERVER_STOP_TIMEOUT)
    }
  }
}