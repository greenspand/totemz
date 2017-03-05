package ro.cluj.totemz.utils

import java.math.BigDecimal

/**
 * Created by sorin on 31.10.16.
 */
fun truncateDecimal(x: Double, numberofDecimals: Int): BigDecimal {
    if (x > 0) {
        return BigDecimal(x.toString()).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR)
    } else {
        return BigDecimal(x.toString()).setScale(numberofDecimals, BigDecimal.ROUND_CEILING)
    }
}