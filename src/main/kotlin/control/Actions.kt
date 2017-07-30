package control

import constants.START_TIMEOUT
import constants.TICK
import constants.TICK_MAX
import java.net.DatagramSocket
import java.net.InetAddress

class Actions(val clientSocket: DatagramSocket, val host: InetAddress, val port: Int) {

  fun move(x: Int, y: Int) {
    sendWithTick("move", "$x $y")
  }

  fun kick(power: Int, direction: Int) {
    sendWithTick("kick", "$power $direction")
  }

  fun dash(power: Int) {
    sendWithTick("dash", "$power")
  }

  fun turn(angle: Int) {
    sendWithTick("turn", "$angle")
  }

  fun turnNeck(angle: Int) {
    send("turn_neck", "$angle")
  }

  fun changeView(width: String, quality: String) {
    send("change_view", "$width $quality")
  }

  fun receive() {
    receiveCommand(clientSocket, "No Command", "")
  }

  private fun sendWithTick(command: String, param: String) {
    sendAndReceiveCommand(clientSocket, command, param, host, port)
    Thread.sleep(TICK)
  }

  private fun send(command: String, param: String) {
    sendAndReceiveCommand(clientSocket, command, param, host, port)
  }

  fun runAndReturn() {
    Thread.sleep(START_TIMEOUT)
    for (i in 0..TICK_MAX / 2) {
      dash(90)
      turn(180)
    }
  }
}

