package diploma.vision

import diploma.constants.server.*
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.log10

fun getViewFrequency(viewWidth: ViewWidth, viewQuality: ViewQuality) =
    SENSE_STEP * viewQuality.getFactor() * viewWidth.getFactor()

fun getViewAngle(viewWidth: ViewWidth) =
    VISIBLE_ANGLE * viewWidth.getFactor()

fun getQuantizeDistance(d: Double): Double =
    quantize(exp(quantize(log10(d), QUANTIZE_STEP)), 0.1)

fun getQuantizeDistanceFlag(d: Double): Double =
    quantize(exp(quantize(log10(d), QUANTIZE_STEP_L)), 0.1)

fun quantize(v: Double, q: Double): Double = ceil(v / q) * q