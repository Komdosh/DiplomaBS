package teams.Trainings

import diploma.constants.server.SERVER_STOP_TIMEOUT
import diploma.control.initTrainer
import diploma.control.sendAndReceiveCommand
import java.net.DatagramSocket
import java.net.InetAddress

class Trainer {
  fun init(): Runnable {
    return Runnable {
      val port: Int
      val host: InetAddress

      val clientSocket: DatagramSocket by lazy { DatagramSocket() }
      try {
        val initData = initTrainer(clientSocket)

        host = initData.address
        port = initData.port

        Thread.sleep(500L)

        sendAndReceiveCommand(clientSocket, "change_mode", "kick_off_l", host, port)

        Thread.sleep(SERVER_STOP_TIMEOUT)
      } catch (e: Exception) {
        println(e.message)
      } finally {
        clientSocket.close()
      }
    }
  }
}
