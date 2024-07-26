package com.github.wigggy.botsbase.systems.analysis

import com.github.wigggy.botsbase.systems.analysis.indicators.*
import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MAType
import com.tictactec.ta.lib.MInteger

object TechnicalAnalysis {

    // Lazy initialization of Core
    private val taLibCore: Core by lazy { Core() }


    fun sma(input: List<Double>, period: Int): SMA {
        val inputArray = input.toDoubleArray()
        val outputArray = DoubleArray(input.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.sma(0, inputArray.size - 1, inputArray, period, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < input.size) {
            result.add(0, 0.0)
        }

        return SMA(input, period, result)
    }


    fun ema(input: List<Double>, period: Int): EMA {
        val inputArray = input.toDoubleArray()
        val outputArray = DoubleArray(input.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.ema(0, inputArray.size - 1, inputArray, period, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < input.size) {
            result.add(0, 0.0)
        }

        return EMA(input, period, result)
    }


    fun wma(input: List<Double>, period: Int): WMA {
        val inputArray = input.toDoubleArray()
        val outputArray = DoubleArray(input.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.wma(0, inputArray.size - 1, inputArray, period, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < input.size) {
            result.add(0, 0.0)
        }

        return WMA(input, period, result)
    }


    // TODO TEST
    fun macd(input: List<Double>, fastPeriod: Int, slowPeriod: Int, signalPeriod: Int): MACD {
        val inputArray = input.toDoubleArray()
        val macd = DoubleArray(input.size)
        val signal = DoubleArray(input.size)
        val hist = DoubleArray(input.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.macd(0, inputArray.size - 1, inputArray,
            fastPeriod, slowPeriod, signalPeriod, begin, length, macd, signal, hist)

        // Convert the output arrays to lists and round values to two decimal places
        val macdResult = macd
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        val signalResult = signal
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        val histResult = hist
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output lists the same size as the input list
        while (macdResult.size < input.size) {
            macdResult.add(0, 0.0)
        }
        while (signalResult.size < input.size) {
            signalResult.add(0, 0.0)
        }
        while (histResult.size < input.size) {
            histResult.add(0, 0.0)
        }

        return MACD(
            input,
            fastPeriod,
            slowPeriod,
            signalPeriod,
            macdResult,
            signalResult,
            histResult
        )
    }


    // TODO TEST
    fun rsi(input: List<Double>, period: Int): RSI {
        val inputArray = input.toDoubleArray()
        val outputArray = DoubleArray(input.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.rsi(0, inputArray.size - 1, inputArray, period, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < input.size) {
            result.add(0, 0.0)
        }

        return RSI(input, period, result)
    }

    
    private fun bollingerBands(input: List<Double>, period: Int, stdMplier: Double, maType: MAType): BollingerBands{
        val inputArray = input.toDoubleArray()
        val upperBand = DoubleArray(input.size)
        val middleBand = DoubleArray(input.size)
        val lowerBand = DoubleArray(input.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.bbands(
            0, inputArray.size - 1, inputArray,
            period, stdMplier, stdMplier, maType,
            begin, length, upperBand, middleBand, lowerBand
        )

        // Convert the output arrays to lists and round values to two decimal places
        val upperBandResult = upperBand
            .map { "%.2f".format(it).toDouble() }
            .map { if (it == 0.0) null else it }
            .filterNotNull()
            .toMutableList()

        val middleBandResult = middleBand
            .map { "%.2f".format(it).toDouble() }
            .map { if (it == 0.0) null else it }
            .filterNotNull()
            .toMutableList()

        val lowerBandResult = lowerBand
            .map { "%.2f".format(it).toDouble() }
            .map { if (it == 0.0) null else it }
            .filterNotNull()
            .toMutableList()

        // Add leading zeros to make the output lists the same size as the input list
        while (upperBandResult.size < input.size) {
            upperBandResult.add(0, 0.0)
        }
        while (middleBandResult.size < input.size) {
            middleBandResult.add(0, 0.0)
        }
        while (lowerBandResult.size < input.size) {
            lowerBandResult.add(0, 0.0)
        }

        return BollingerBands(
            input, period, upperBandResult, middleBandResult, lowerBandResult
        )
    }


    // TODO TEST
    fun bollingBandsSmaMiddleBand(input: List<Double>, period: Int = 20, stdMplier: Double = 2.0): BollingerBands =
        bollingerBands(input, period, stdMplier, MAType.Sma)


    // TODO TEST
    fun bollingBandsEmaMiddleBand(input: List<Double>, period: Int = 20, stdMplier: Double = 2.0): BollingerBands =
        bollingerBands(input, period, stdMplier, MAType.Ema)


    // TODO TEST
    fun bollingBandsWmaMiddleBand(input: List<Double>, period: Int = 20, stdMplier: Double = 2.0): BollingerBands =
        bollingerBands(input, period, stdMplier, MAType.Wma)



    // TODO TEST
    fun standardDeviation(input: List<Double>, period: Int, multiplier: Double): StandardDeviation {
        val inputArray = input.toDoubleArray()
        val outputArray = DoubleArray(input.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.stdDev(0, inputArray.size - 1, inputArray,
            period, multiplier, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < input.size) {
            result.add(0, 0.0)
        }

        return StandardDeviation(input, period, multiplier, result)
    }


    // TODO TEST
    fun parabolicSAR(
        high: List<Double>,
        low: List<Double>,
        accelerationStep: Double,
        maximumStep: Double
    ): ParabolicSAR {
        val highArray = high.toDoubleArray()
        val lowArray = low.toDoubleArray()
        val outputArray = DoubleArray(high.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.sar(0, highArray.size - 1, highArray, lowArray,
            accelerationStep, maximumStep, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < high.size) {
            result.add(0, 0.0)
        }

        return ParabolicSAR(
            high, low, accelerationStep, maximumStep, result
        )
    }


    // TODO TEST
    fun averageTrueRange(
        high: List<Double>,
        low: List<Double>,
        close: List<Double>,
        period: Int
    ): AverageTrueRange {
        val highArray = high.toDoubleArray()
        val lowArray = low.toDoubleArray()
        val closeArray = close.toDoubleArray()
        val outputArray = DoubleArray(high.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.atr(0, highArray.size - 1,
            highArray, lowArray, closeArray,
            period, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < high.size) {
            result.add(0, 0.0)
        }

        return AverageTrueRange(high, low, close, period, result)
    }


    // TODO TEST
    fun directionalMovementIndex(
        high: List<Double>,
        low: List<Double>,
        close: List<Double>,
        period: Int
    ): DirectionalMovmentIndex {
        val highArray = high.toDoubleArray()
        val lowArray = low.toDoubleArray()
        val closeArray = close.toDoubleArray()

        val plusDIArray = DoubleArray(high.size)
        val minusDIArray = DoubleArray(high.size)
        val adxArray = DoubleArray(high.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.plusDI(0, highArray.size - 1, highArray, lowArray, closeArray,
            period, begin, length, plusDIArray)
        taLibCore.minusDI(0, highArray.size - 1, highArray, lowArray, closeArray,
            period, begin, length, minusDIArray)
        taLibCore.adx(0, highArray.size - 1, highArray, lowArray, closeArray,
            period, begin, length, adxArray)

        // Convert the output arrays to lists and round values to two decimal places
        val plusDIResult = plusDIArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        val minusDIResult = minusDIArray
            .map { "%.2f".format(it).toDouble() }
            .map { if (it == 0.0) null else it }
            .filterNotNull()
            .toMutableList()

        val adxResult = adxArray
            .map { "%.2f".format(it).toDouble() }
            .map { if (it == 0.0) null else it }
            .filterNotNull()
            .toMutableList()

        // Add leading zeros to make the output lists the same size as the input list
        while (plusDIResult.size < high.size) {
            plusDIResult.add(0, 0.0)
        }
        while (minusDIResult.size < high.size) {
            minusDIResult.add(0, 0.0)
        }
        while (adxResult.size < high.size) {
            adxResult.add(0, 0.0)
        }

        return DirectionalMovmentIndex(high, low, close, period, plusDIResult, minusDIResult, adxResult)
    }


    // TODO TEST
    private fun calculatePPO(
        input: List<Double>,
        fastPeriod: Int,
        slowPeriod: Int,
        maType: MAType
    ): PercentagePriceOscillator {
        val inputArray = input.toDoubleArray()
        val outputArray = DoubleArray(input.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.ppo(0, inputArray.size - 1, inputArray,
            fastPeriod, slowPeriod, maType, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < input.size) {
            result.add(0, 0.0)
        }

        return PercentagePriceOscillator(input, fastPeriod, slowPeriod, result)
    }


    // TODO TEST
    fun percentagePriceOscillatorWithSma(
        input: List<Double>,
        fastPeriod: Int,
        slowPeriod: Int,
    ): PercentagePriceOscillator = calculatePPO(input, fastPeriod, slowPeriod, MAType.Sma)


    // TODO TEST
    fun percentagePriceOscillatorWithEma(
        input: List<Double>,
        fastPeriod: Int,
        slowPeriod: Int,
    ): PercentagePriceOscillator = calculatePPO(input, fastPeriod, slowPeriod, MAType.Ema)


    // TODO TEST
    fun percentagePriceOscillatorWithWma(
        input: List<Double>,
        fastPeriod: Int,
        slowPeriod: Int,
    ): PercentagePriceOscillator = calculatePPO(input, fastPeriod, slowPeriod, MAType.Wma)


    // TODO TEST
    fun onBalanceVolume(close: List<Double>, volume: List<Int>): OnBalanceVolume {
        val closeArray = close.toDoubleArray()
        val volumeArray = volume.map { it.toDouble() }.toDoubleArray()
        val outputArray = DoubleArray(close.size)

        val begin = MInteger()
        val length = MInteger()

        taLibCore.obv(0, closeArray.size - 1, closeArray, volumeArray, begin, length, outputArray)

        // Convert the output array to a list and round values to two decimal places
        val result = outputArray
            .map { "%.2f".format(it).toDouble() }  // Round to two decimal places
            .map { if (it == 0.0) null else it }   // Remove trailing zeros
            .filterNotNull()                       // Remove nulls from the list
            .toMutableList()

        // Add leading zeros to make the output list the same size as the input list
        while (result.size < close.size) {
            result.add(0, 0.0)
        }

        return OnBalanceVolume(close, volume, result)
    }
}