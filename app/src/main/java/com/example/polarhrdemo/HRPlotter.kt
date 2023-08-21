package com.example.polarhrdemo

import android.graphics.Color
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYSeriesFormatter
import com.polar.sdk.api.model.PolarHrData
import java.util.*

/**
 * Implements two series for HR and RR using time for the x values.
 */
class HRPlotter {
    companion object {
        private const val TAG = "TimePlotter"
        private const val NVALS = 300 // 5 min
        private const val RR_SCALE = .1
    }

    private var listener: UpdateCallback? = null
    val hrFormatter: XYSeriesFormatter<*>
    val hrSeries: SimpleXYSeries
    private val xHrVals = MutableList(NVALS) { 0.0 }
    private val yHrVals = MutableList(NVALS) { 0.0 }
    private val xRrVals = MutableList(NVALS) { 0.0 }
    private val yRrVals = MutableList(NVALS) { 0.0 }

    init {
        val now = Date()
        val endTime = now.time.toDouble()
        val startTime = endTime - NVALS * 1000
        val delta = (endTime - startTime) / (NVALS - 1)

        // Specify initial values to keep it from auto sizing
        for (i in 0 until NVALS) {
            xHrVals[i] = startTime + i * delta
            yHrVals[i] = 60.0
            xRrVals[i] = startTime + i * delta
            yRrVals[i] = 100.0
        }
        hrFormatter = LineAndPointFormatter(Color.RED, null, null, null)
        hrFormatter.setLegendIconEnabled(false)
        hrSeries = SimpleXYSeries(xHrVals, yHrVals, "HR")
    }

    /**
     * Implements a strip chart by moving series data backwards and adding
     * new data at the end.
     *
     * @param polarHrData The HR data that came in.
     */
    fun addValues(polarHrData: PolarHrData.PolarHrSample) {
        val now = Date()
        val time = now.time
        for (i in 0 until NVALS - 1) {
            xHrVals[i] = xHrVals[i + 1]
            yHrVals[i] = yHrVals[i + 1]
            hrSeries.setXY(xHrVals[i], yHrVals[i], i)
        }
        xHrVals[NVALS - 1] = time.toDouble()
        yHrVals[NVALS - 1] = polarHrData.hr.toDouble()
        hrSeries.setXY(xHrVals[NVALS - 1], yHrVals[NVALS - 1], NVALS - 1)
        listener?.updateGraph()
    }

    fun setListener(listener: UpdateCallback?) {
        this.listener = listener
    }
}