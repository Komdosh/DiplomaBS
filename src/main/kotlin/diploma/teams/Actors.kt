package diploma.teams

import diploma.constants.server.SERVER_STOP_TIMEOUT
import diploma.control.Action
import diploma.control.initPayer
import java.net.DatagramSocket
import java.net.InetAddress

abstract class Actors {
  protected val actors: MutableList<Thread> = ArrayList()

  abstract fun getActorThreads(): List<Thread>

  fun sillyRotateActor(config: PlayerConfig, teamName: String): Runnable {
    return initPayer(teamName) { clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action = Action(clientSocket, host, port)
      action.move(config.initialX, config.initialY)

      action.runAndReturn()
      Thread.sleep(SERVER_STOP_TIMEOUT)
    }
  }

  fun sillySpyActor(config: PlayerConfig, teamName: String): Runnable {
    return initPayer(teamName) { clientSocket: DatagramSocket, host: InetAddress, port: Int ->

      val action = Action(clientSocket, host, port)
      action.move(config.initialX, config.initialY)

      action.runAhead()
      Thread.sleep(SERVER_STOP_TIMEOUT)
    }
  }
}