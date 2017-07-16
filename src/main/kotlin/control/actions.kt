package control

import control.sendAndReceiveCommand
import java.net.DatagramSocket
import java.net.InetAddress

class Actions(val clientSocket: DatagramSocket, val host: InetAddress, val port: Int) {

  fun move(param: String) {
    sendAndReceiveCommand(clientSocket, "move", param, host, port)
  }

  fun dash(param: String){
    sendAndReceiveCommand(clientSocket, "dash", param, host, port)
  }
}

