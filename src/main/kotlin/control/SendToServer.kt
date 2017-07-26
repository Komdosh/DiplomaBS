package control

import constants.IP_ADDRESS
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun initPayer(teamName: String, commands: (clientSocket: DatagramSocket, host: InetAddress, port: Int) -> Unit): Runnable{
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

@Throws(Exception::class)
fun sendAndReceiveCommand(clientSocket: DatagramSocket, command: String, params: String,
                          host: InetAddress, port: Int): DatagramPacket {
  sendDataToServer(command, params, host, port, clientSocket)
  return receiveDataFromServer(clientSocket, command, params)
}

private fun sendDataToServer(command: String, params: String, IPAddress: InetAddress, port: Int, clientSocket: DatagramSocket) {
  val sendData: ByteArray = "($command $params)".toByteArray()
  val packetToSend = DatagramPacket(sendData, sendData.size, IPAddress, port)
  clientSocket.send(packetToSend)
}

private fun receiveDataFromServer(clientSocket: DatagramSocket, command: String, params: String): DatagramPacket {
  val receiveData = ByteArray(1024)
  val receivePacket = DatagramPacket(receiveData, receiveData.size)
  clientSocket.receive(receivePacket)

  writeAnswerFromServer(receivePacket, command, params)
  return receivePacket
}

private fun writeAnswerFromServer(receivePacket: DatagramPacket, command: String, params: String) {
  val modifiedSentence = String(receivePacket.data, 0, receivePacket.length - 1)
  println("FROM SERVER ($command $params):$modifiedSentence")
}
