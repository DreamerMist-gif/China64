package com.example.rustiching

import android.content.Context
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
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    // 加载 Rust 编译库
    companion object {
        init {
            System.loadLibrary("rust_iching")
        }
    }

    // 声明 Rust 函数
    external fun getDivination(azimuth: Double, timestamp: Long, acceleration: Double): String

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

        // 简单的代码构建 UI (无需 XML)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            padding = 50
            gravity = Gravity.CENTER_HORIZONTAL
        }
        
        infoText = TextView(this).apply {
            text = "正在校准传感器..."
            textSize = 16f
            setPadding(0, 0, 0, 50)
        }

        val button = Button(this).apply {
            text = "感 应 起 卦"
            setOnClickListener {
                performDivination()
            }
        }

        resultText = TextView(this).apply {
            textSize = 18f
            setPadding(0, 50, 0, 0)
            typeface = android.graphics.Typeface.MONOSPACE // 等宽字体对齐卦象
        }

        val scrollView = ScrollView(this).apply {
            addView(resultText)
        }

        layout.addView(infoText)
        layout.addView(button)
        layout.addView(scrollView)
        setContentView(layout)

        // 初始化传感器
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
            // 计算总加速度向量长度作为“运动状态”熵
            currentAccel = sqrt((gravity[0]*gravity[0] + gravity[1]*gravity[1] + gravity[2]*gravity[2]).toDouble())
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        }

        val R = FloatArray(9)
        val I = FloatArray(9)
        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            // orientation[0] 是方位角 (弧度)，转为度数
            currentAzimuth = (Math.toDegrees(orientation[0].toDouble()) + 360) % 360
        }
        
        infoText.text = "方位: %.1f° | 动能: %.2f".format(currentAzimuth, currentAccel)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun performDivination() {
        val timestamp = System.currentTimeMillis()
        // 调用 Rust
        val result = getDivination(currentAzimuth, timestamp, currentAccel)
        resultText.text = result
    }
}

// 扩展属性用于设置 padding
fun android.view.View.padding(p: Int) = setPadding(p,p,p,p)
