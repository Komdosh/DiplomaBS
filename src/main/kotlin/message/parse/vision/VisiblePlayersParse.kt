package message.parse.vision

import model.VisiblePlayer
import kotlin.streams.toList

fun parseVisiblePlayers(message: String): List<VisiblePlayer> {
  val teamNameRegex: Regex = "\\\"[A-Za-z0-9]+\\\"".toRegex()
  val playerNumberRegex: Regex = "\\d+\\)?".toRegex()
  val unknownRegex: Regex = "\\).+?\\d\\)".toRegex()
  val playersRegex: Regex = "\\(\\([p]\\s$teamNameRegex\\s\\d+\\).+?\\d\\)".toRegex()

  val tickRegex: Regex = "\\(see\\s\\d+".toRegex()
  val tick = tickRegex.find(message)?.groupValues?.get(0)?.removePrefix("(see ")?.toInt()

  return playersRegex.findAll(message).map {
    it.groupValues[0]
  }.toList().parallelStream().map {
    val teamName = getTeamName(teamNameRegex, it)
    val playerNumber = getPlayerNumber(playerNumberRegex, it)
    val unknown = getUnknownParams(unknownRegex, it)

    VisiblePlayer(teamName, playerNumber, unknown, tick)
  }.toList()
}

private fun getUnknownParams(unknownRegex: Regex, it: String) =
    unknownRegex.find(it)!!.groupValues[0].removeSurrounding(")").trim().split(" ").map { it.toInt() }.toTypedArray()

private fun getPlayerNumber(playerNumberRegex: Regex, it: String) =
    playerNumberRegex.find(it)!!.groupValues[0].removeSuffix(")").toInt()

private fun getTeamName(teamNameRegex: Regex, it: String) =
    teamNameRegex.find(it)!!.groupValues[0].removeSurrounding("\"")