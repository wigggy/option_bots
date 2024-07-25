import kotlin.math.round
import kotlin.random.Random

fun getRandomDouble(): Double {
    val randomValue = Random.nextDouble(0.00, 0.10)
    return round(randomValue * 100) / 100
}

fun main() {
    val randomDouble = getRandomDouble()
    println(randomDouble)
}