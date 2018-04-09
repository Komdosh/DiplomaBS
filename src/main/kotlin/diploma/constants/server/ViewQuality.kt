package diploma.constants.server

enum class ViewQuality(private val quality: String) {
  LOW("low"), HIGH("low");

  fun getFactor(): Double {
    return when (this) {
      LOW -> 0.5
      HIGH -> 1.0
    }
  }

  override fun toString(): String = quality
}