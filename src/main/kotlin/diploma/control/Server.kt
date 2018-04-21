package diploma.control

import diploma.constants.server.IP_ADDRESS
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun initPayer(teamName: String, commands: (clientSocket: DatagramSocket, host: InetAddress, port: Int) -> Unit): Runnable {
  return Runnable {
    val port: Int
    val host: InetAddress

    val clientSocket: DatagramSocket by lazy { DatagramSocket() }
    try {
      val initData = init(clientSocket, teamName)

      host = initData.address
      port = initData.port

      commands(clientSocket, host, port)
    } catch (e: Exception) {
      println(e.message)
    } finally {
      clientSocket.close()
    }
  }
}

private fun init(clientSocket: DatagramSocket, teamName: String): DatagramPacket {
  return sendAndReceiveCommand(clientSocket, "init $teamName", "(version 15)", IP_ADDRESS, 6000)
}

fun initTrainer(clientSocket: DatagramSocket): DatagramPacket {
  return sendAndReceiveCommand(clientSocket, "init", "(version 15)", IP_ADDRESS, 6001)
}

fun sendAndReceiveCommand(clientSocket: DatagramSocket, command: String, params: String,
                          host: InetAddress, port: Int): DatagramPacket {
  sendDataToServer(command, params, host, port, clientSocket)
  return receiveDataFromServer(clientSocket)
}

fun receiveCommand(clientSocket: DatagramSocket): DatagramPacket {
  return receiveDataFromServer(clientSocket)
}

private fun sendDataToServer(command: String, params: String, IPAddress: InetAddress, port: Int, clientSocket: DatagramSocket) {
  val param = StringBuilder("")
  if (!params.isBlank()) {
    param.append(" ").append(params)
  }
  val sendData: ByteArray = "($command$param)".toByteArray()
  val packetToSend = DatagramPacket(sendData, sendData.size, IPAddress, port)
  clientSocket.send(packetToSend)
}

fun receiveDataFromServer(clientSocket: DatagramSocket): DatagramPacket {
  val receiveData = ByteArray(1024)
  val receivePacket = DatagramPacket(receiveData, receiveData.size)
  clientSocket.receive(receivePacket)
  return receivePacket
}
