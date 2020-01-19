package diploma.vision

import diploma.model.VisiblePlayer
import diploma.model.VisiblePlayerExtInfo
import java.net.DatagramPacket
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
        val detailedInfo = getUnknownParams(unknownRegex, it)
        val distance = if (detailedInfo.size == 1 || detailedInfo.isEmpty()) null else detailedInfo[0]
        val direction = if (detailedInfo.size == 1) detailedInfo[0] else if (detailedInfo.isNotEmpty()) detailedInfo[1] else null

        val visiblePlayerExtInfo: VisiblePlayerExtInfo? = if (detailedInfo.size > 2) getVisiblePlayerExtInfo(detailedInfo) else null

        VisiblePlayer(teamName, playerNumber, direction, distance, visiblePlayerExtInfo, tick)
    }.toList()
}

fun isMessageSee(message: String): Boolean {
    return message.contains("see", true)
}

fun getServerMessage(receivePacket: DatagramPacket): String {
    return String(receivePacket.data, 0, receivePacket.length - 1)
}

private fun getVisiblePlayerExtInfo(detailedInfo: Array<Int?>): VisiblePlayerExtInfo? {
    val distChange = detailedInfo[2]
    val dirChange = detailedInfo[3]

    val bodyFacingDir = detailedInfo[4]
    val headFacingDir = detailedInfo[5]

    return VisiblePlayerExtInfo(distChange, dirChange, bodyFacingDir, headFacingDir)
}

private fun getUnknownParams(unknownRegex: Regex, message: String) =
        firstGroupValue(unknownRegex, message).removeSurrounding(")").trim().split(" ").map { it.toIntOrNull() }.toTypedArray()

private fun getPlayerNumber(playerNumberRegex: Regex, message: String) =
        firstGroupValue(playerNumberRegex, message).removeSuffix(")").toInt()

private fun getTeamName(teamNameRegex: Regex, message: String) =
        firstGroupValue(teamNameRegex, message).removeSurrounding("\"")

private fun firstGroupValue(regex: Regex, message: String) =
        regex.find(message)?.groupValues?.first() ?: error("No value for $message by $regex")
