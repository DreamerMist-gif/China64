package com.example.iching

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private var currentAzimuth: Double = 0.0
    private var currentAccel: Double = 0.0
    private lateinit var infoText: TextView
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dynamically build UI (No XML required)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        val title = TextView(this).apply {
            text = "周易 · 感应起卦"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 40)
        }

        infoText = TextView(this).apply {
            text = "正在采集环境磁场与动量..."
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 60)
        }

        val button = Button(this).apply {
            text = "诚 心 占 卜"
            textSize = 18f
            setOnClickListener { performDivination() }
        }

        resultText = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 60, 0, 0)
            typeface = Typeface.MONOSPACE
        }

        val scrollView = ScrollView(this).apply { addView(resultText) }

        layout.addView(title)
        layout.addView(infoText)
        layout.addView(button)
        layout.addView(scrollView)
        setContentView(layout)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
            val x = gravity[0]; val y = gravity[1]; val z = gravity[2]
            currentAccel = sqrt((x*x + y*y + z*z).toDouble())
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        }
        val R = FloatArray(9)
        val I = FloatArray(9)
        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            var degrees = Math.toDegrees(orientation[0].toDouble())
            if (degrees < 0) degrees += 360.0
            currentAzimuth = degrees
        }
        infoText.text = "方位: %.1f°\n动能: %.3f".format(currentAzimuth, currentAccel)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun performDivination() {
        val timestamp = System.currentTimeMillis()
        val seed = (currentAzimuth * 100).toLong().hashCode().toLong() + timestamp + (currentAccel * 1000).toLong().hashCode().toLong()
        val rng = Random(seed)
        val hexagramNums = IntArray(6)
        
        for (i in 0 until 6) {
            val sum = rng.nextInt(2) + rng.nextInt(2) + rng.nextInt(2)
            hexagramNums[i] = when(sum) { 0 -> 6; 1 -> 7; 2 -> 8; 3 -> 9; else -> 7 }
        }

        val sb = StringBuilder()
        sb.append("种子: $seed\n时间: $timestamp\n\n【卦象】\n")
        
        for (i in 5 downTo 0) {
            val yao = hexagramNums[i]
            val graph = when(yao) {
                6 -> "——  —— X (老阴)"
                7 -> "——————   (少阳)"
                8 -> "——  ——   (少阴)"
                9 -> "—————— O (老阳)"
                e            orientation = LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        val title = TextView(this).apply {
            text = "周易 · 感应起卦"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 40)
        }

        infoText = TextView(this).apply {
            text = "正在采集环境磁场与动量..."
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 60)
        }

        val button = Button(this).apply {
            text = "诚 心 占 卜"
            textSize = 18f
            setOnClickListener { performDivination() }
        }

        resultText = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 60, 0, 0)
            typeface = Typeface.MONOSPACE
        }

        val scrollView = ScrollView(this).apply { addView(resultText) }

        layout.addView(title)
        layout.addView(infoText)
        layout.addView(button)
        layout.addView(scrollView)
        setContentView(layout)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
            val x = gravity[0]; val y = gravity[1]; val z = gravity[2]
            currentAccel = sqrt((x*x + y*y + z*z).toDouble())
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        }
        val R = FloatArray(9)
        val I = FloatArray(9)
        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            var degrees = Math.toDegrees(orientation[0].toDouble())
            if (degrees < 0) degrees += 360.0
            currentAzimuth = degrees
        }
        infoText.text = "方位: %.1f°\n动能: %.3f".format(currentAzimuth, currentAccel)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun performDivination() {
        val timestamp = System.currentTimeMillis()
        val seed = (currentAzimuth * 100).toLong().hashCode().toLong() + timestamp + (currentAccel * 1000).toLong().hashCode().toLong()
        val rng = Random(seed)
        val hexagramNums = IntArray(6)
        
        for (i in 0 until 6) {
            val sum = rng.nextInt(2) + rng.nextInt(2) + rng.nextInt(2)
            hexagramNums[i] = when(sum) { 0 -> 6; 1 -> 7; 2 -> 8; 3 -> 9; else -> 7 }
        }

        val sb = StringBuilder()
        sb.append("种子: $seed\n时间: $timestamp\n\n【卦象】\n")
        
        for (i in 5 downTo 0) {
            val yao = hexagramNums[i]
            val graph = when(yao) {
                6 -> "——  —— X (老阴)"
                7 -> "——————   (少阳)"
                8 -> "——  ——   (少阴)"
                9 -> "—————— O (老阳)"
                e
