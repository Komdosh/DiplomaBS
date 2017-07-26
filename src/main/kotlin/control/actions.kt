package control

import constants.START_TIMEOUT
import constants.TICK
import constants.TICK_MAX
import java.net.DatagramSocket
import java.net.InetAddress

class Actions(val clientSocket: DatagramSocket, val host: InetAddress, val port: Int) {

  fun move(x: Int, y: Int) {
    send("move", "$x $y")
  }

  fun kick(power: Int, direction: Int) {
    send("kick", "$power $direction")
  }

  fun dash(power: Int) {
    send("dash", "$power")
  }

  fun turn(angle: Int) {
    send("turn", "$angle")
  }

  private fun send(command: String, param: String) {
    sendAndReceiveCommand(clientSocket, command, param, host, port)
    Thread.sleep(TICK)
  }

  fun runAndReturn() {
    Thread.sleep(START_TIMEOUT)
    for (i in 0..TICK_MAX / 2) {
      dash(90)
      turn(180)
    }
  }
}

