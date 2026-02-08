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
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    
    // 实时传感器数据
    private var currentAzimuth: Double = 0.0
    private var currentAccel: Double = 0.0

    private lateinit var infoText: TextView
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. 纯代码构建 UI ---
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#F5F5F5")) // 淡灰背景
        }

        val title = TextView(this).apply {
            text = "易 · 感应起卦"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 40)
        }

        infoText = TextView(this).apply {
            text = "正在采集环境磁场与引力..."
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 60)
        }

        val button = Button(this).apply {
            text = "诚 心 占 卜"
            textSize = 18f
            setPadding(30, 20, 30, 20)
            setOnClickListener { performDivination() }
        }

        resultText = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 60, 0, 0)
            // 使用等宽字体以确保卦象对齐
            typeface = Typeface.MONOSPACE
        }

        val scrollView = ScrollView(this).apply {
            addView(resultText)
        }

        layout.addView(title)
        layout.addView(infoText)
        layout.addView(button)
        layout.addView(scrollView)
        setContentView(layout)

        // --- 2. 初始化传感器 ---
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

    // --- 3. 传感器数据处理 ---
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
            // 计算合加速度，作为“动”的变量
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
            // 将弧度转换为角度 (0-360)
            var degrees = Math.toDegrees(orientation[0].toDouble())
            if (degrees < 0) degrees += 360.0
            currentAzimuth = degrees
        }

        infoText.text = "方位: %.1f°\n动能: %.3f".format(currentAzimuth, currentAccel)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // --- 4. 核心起卦逻辑 ---
    private fun performDivination() {
        val timestamp = System.currentTimeMillis()
        
        // 步骤 A: 生成随机种子 (Seed)
        // 将 指南针、时间、加速度 混合
        // 使用简单的位操作和HashCode来混合生成一个Long类型的种子
        val seed = (currentAzimuth * 100).toLong().hashCode().toLong() + 
                   timestamp + 
                   (currentAccel * 1000).toLong().hashCode().toLong()

        // 步骤 B: 初始化随机数生成器
        val rng = Random(seed)

        // 步骤 C: 生成六爻 (6,7,8,9)
        // 模拟金钱卦：三枚铜钱
        // 正面(字)=0 (概率1/2), 背面(花)=1 (概率1/2) 
        // 实际上：
        // 3背 (1+1+1=3) -> 老阳(9)
        // 3正 (0+0+0=0
