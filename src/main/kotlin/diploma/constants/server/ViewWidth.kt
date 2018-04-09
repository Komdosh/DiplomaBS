package diploma.constants.server

enum class ViewWidth(private val width: String) {
  NARROW("narrow"), NORMAL("normal"), WIDE("wide");

  fun getFactor(): Double {
    return when (this) {
      NARROW -> 0.5
      NORMAL -> 1.0
      WIDE -> 2.0
    }
  }

  override fun toString(): String = width
}