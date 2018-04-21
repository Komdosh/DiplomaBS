package diploma.control

import diploma.constants.START_TIMEOUT
import diploma.constants.TICK_MAX
import diploma.constants.server.TICK
import diploma.constants.server.ViewQuality
import diploma.constants.server.ViewWidth
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class Action(private val clientSocket: DatagramSocket, private val host: InetAddress, private val port: Int) {

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

  fun changeView(width: ViewWidth, quality: ViewQuality) {
    send("change_view", "$width $quality")
  }

  fun receive(): DatagramPacket {
    return receiveCommand(clientSocket)
  }

  private fun sendWithTick(command: String, param: String) {
    sendAndReceiveCommand(clientSocket, command, param, host, port)
    Thread.sleep(TICK.toLong())
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

