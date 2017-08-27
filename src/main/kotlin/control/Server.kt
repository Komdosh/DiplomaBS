package control

import constants.IP_ADDRESS
import constants.MAIN_ACTOR
import message.parse.vision.parseVisiblePlayers
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

fun sendAndReceiveCommand(clientSocket: DatagramSocket, command: String, params: String,
                          host: InetAddress, port: Int): DatagramPacket {
  sendDataToServer(command, params, host, port, clientSocket)
  return receiveDataFromServer(clientSocket, command, params)
}

fun receiveCommand(clientSocket: DatagramSocket, command: String, params: String): DatagramPacket {
  return receiveDataFromServer(clientSocket, command, params)
}

private fun sendDataToServer(command: String, params: String, IPAddress: InetAddress, port: Int, clientSocket: DatagramSocket) {
  val param: StringBuilder = StringBuilder("")
  if (!params.isBlank()) {
    param.append(" ").append(params)
  }
  val sendData: ByteArray = "($command$param)".toByteArray()
  val packetToSend = DatagramPacket(sendData, sendData.size, IPAddress, port)
  clientSocket.send(packetToSend)
}

fun receiveDataFromServer(clientSocket: DatagramSocket, command: String, params: String): DatagramPacket {
  val receiveData = ByteArray(1024)
  val receivePacket = DatagramPacket(receiveData, receiveData.size)
  clientSocket.receive(receivePacket)

  printlnAnswerFromServer(receivePacket, command, params)
  return receivePacket
}

private fun printlnAnswerFromServer(receivePacket: DatagramPacket, command: String, params: String) {
  val modifiedSentence: String = String(receivePacket.data, 0, receivePacket.length - 1)
  val param: StringBuilder = StringBuilder("")
  if (!params.isBlank()) {
    param.append(" ").append(params)
  }
  if (!modifiedSentence.contains("warning", true) && !modifiedSentence.contains("error", true)) {
    //println("FROM SERVER ($command$param):$modifiedSentence")

    if (modifiedSentence.contains("(p \"", true) && Thread.currentThread().name == MAIN_ACTOR) {
      println(parseVisiblePlayers(modifiedSentence))
    }
  }
}
