package model

import java.util.*

data class VisiblePlayer(private val teamName: String?, private val playerNumber: Int, private val unknown: Array<Int>, private val tick: Int?) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as VisiblePlayer

    if (teamName != other.teamName) return false
    if (playerNumber != other.playerNumber) return false
    if (!Arrays.equals(unknown, other.unknown)) return false
    if (tick != other.tick) return false

    return true
  }

  override fun hashCode(): Int {
    var result = teamName?.hashCode() ?: 0
    result = 31 * result + playerNumber
    result = 31 * result + Arrays.hashCode(unknown)
    result = 31 * result + (tick ?: 0)
    return result
  }


}