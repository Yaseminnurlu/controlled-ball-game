package com.example.controlled_ball_game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TiltGame(this)
        }
    }
}

@Composable
fun TiltGame(context: Context) {
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    tiltX = it.values[0]
                    tiltY = it.values[1]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    var ballX by remember { mutableStateOf(150f) }
    var ballY by remember { mutableStateOf(150f) }

    val ballRadius = 25f
    val speed = 6f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val thickness = 80f

        val walls = mutableListOf<Rect>()

        walls.add(Rect(0f, 0f, width, thickness))

        walls.add(Rect(0f, height - thickness, width, height))

        walls.add(Rect(0f, height / 2, thickness, height))

        walls.add(Rect(width - thickness, 0f, width, height / 2))


        walls.add(Rect(
            width / 3,
            thickness,
            width / 3 + thickness,
            height * 0.65f
        ))

        walls.add(Rect(
            2 * width / 3 - thickness,
            height * 0.35f,
            2 * width / 3,
            height - thickness
        ))

        val nextX = ballX + (-tiltX * speed)
        val nextY = ballY + (tiltY * speed)

        var collision = false

        for (wall in walls) {
            if (nextX + ballRadius > wall.left &&
                nextX - ballRadius < wall.right &&
                nextY + ballRadius > wall.top &&
                nextY - ballRadius < wall.bottom
            ) {
                collision = true
                break
            }
        }

        if (!collision) {
            ballX = nextX
            ballY = nextY
        }

        ballX = max(ballRadius, min(width - ballRadius, ballX))
        ballY = max(ballRadius, min(height - ballRadius, ballY))

        walls.forEach {
            drawRect(Color.Black, topLeft = Offset(it.left, it.top), size = it.size)
        }

        drawCircle(Color.Red, ballRadius, Offset(ballX, ballY))
    }
}
