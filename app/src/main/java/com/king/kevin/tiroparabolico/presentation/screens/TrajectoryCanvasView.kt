package com.king.kevin.tiroparabolico.presentation.screens

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.king.kevin.tiroparabolico.R
import com.king.kevin.tiroparabolico.domain.model.TrajectoryPoint
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class TrajectoryCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.axis_color)
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.trajectory_color)
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }
    private val projectilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.projectile_color)
        style = Paint.Style.FILL
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grid_color)
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
    }
    private val backgroundPathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.trajectory_color)
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
        alpha = 60
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private var trajectory: List<TrajectoryPoint> = emptyList()
    private var animationProgress: Float = 1f
    private var animator: ValueAnimator? = null
    private var onPointSelected: ((TrajectoryPoint) -> Unit)? = null

    private val fullPath = Path()
    private val animatedPath = Path()

    fun setOnPointSelectedListener(listener: (TrajectoryPoint) -> Unit) {
        onPointSelected = listener
    }

    fun submitTrajectory(points: List<TrajectoryPoint>, flightTimeSeconds: Double) {
        trajectory = points
        animator?.cancel()
        animationProgress = 0f
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = (flightTimeSeconds * 1000).toLong().coerceAtLeast(100L)
            interpolator = LinearInterpolator()
            addUpdateListener {
                animationProgress = it.animatedValue as Float
                notifyCurrentPoint()
                invalidate()
            }
            start()
        }
    }

    private fun notifyCurrentPoint() {
        if (trajectory.isEmpty()) return
        val floatIndex = animationProgress * (trajectory.size - 1)
        val i = floatIndex.toInt()
        
        if (i < trajectory.size - 1) {
            val alpha = (floatIndex - i).toDouble()
            val p1 = trajectory[i]
            val p2 = trajectory[i + 1]
            
            val interpolatedPoint = TrajectoryPoint(
                time = p1.time + (p2.time - p1.time) * alpha,
                x = p1.x + (p2.x - p1.x) * alpha,
                y = p1.y + (p2.y - p1.y) * alpha,
                instantaneousVelocity = p1.instantaneousVelocity + (p2.instantaneousVelocity - p1.instantaneousVelocity) * alpha,
                instantaneousAngle = p1.instantaneousAngle + (p2.instantaneousAngle - p1.instantaneousAngle) * alpha
            )
            onPointSelected?.invoke(interpolatedPoint)
        } else {
            onPointSelected?.invoke(trajectory.last())
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (trajectory.isEmpty()) return false
        
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                performClick()
                animator?.cancel()
                updateProgressFromTouch(event.x)
                return true
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                updateProgressFromTouch(event.x)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateProgressFromTouch(touchX: Float) {
        val left = paddingLeft + 70f
        val right = width - paddingRight - 30f
        val x = touchX.coerceIn(left, right)
        animationProgress = (x - left) / (right - left)
        notifyCurrentPoint()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val left = paddingLeft + 70f
        val right = width - paddingRight - 30f
        val top = paddingTop + 30f
        val bottom = height - paddingBottom - 60f
        
        val maxX = if (trajectory.isNotEmpty()) max(trajectory.maxOf { it.x }, 1.0) else 10.0
        val maxY = if (trajectory.isNotEmpty()) max(trajectory.maxOf { it.y }, 1.0) else 10.0

        drawGrid(canvas, left, right, top, bottom, maxX, maxY)
        drawAxes(canvas, left, right, top, bottom)
        
        if (trajectory.isEmpty()) return
        
        // Draw full trajectory path with lower alpha or different style
        val fullPath = Path()
        trajectory.forEachIndexed { index, point ->
            val px = left + ((point.x / maxX) * (right - left)).toFloat()
            val py = bottom - ((point.y / maxY) * (bottom - top)).toFloat()
            if (index == 0) fullPath.moveTo(px, py) else fullPath.lineTo(px, py)
        }
        val backgroundPathPaint = Paint(pathPaint).apply { alpha = 60 }
        canvas.drawPath(fullPath, backgroundPathPaint)

        // Draw animated/selected path
        val floatIndex = animationProgress * (trajectory.size - 1)
        val visibleCount = (floatIndex.toInt() + 1).coerceAtMost(trajectory.size)
        
        val path = Path()
        for (i in 0 until visibleCount) {
            val point = trajectory[i]
            val px = left + ((point.x / maxX) * (right - left)).toFloat()
            val py = bottom - ((point.y / maxY) * (bottom - top)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        
        // Interpolate the very last point for perfect fluidity
        if (floatIndex > 0 && floatIndex < trajectory.size - 1) {
            val i = floatIndex.toInt()
            val alpha = floatIndex - i
            val p1 = trajectory[i]
            val p2 = trajectory[i + 1]
            
            val interX = p1.x + (p2.x - p1.x) * alpha
            val interY = p1.y + (p2.y - p1.y) * alpha
            
            val px = left + ((interX / maxX) * (right - left)).toFloat()
            val py = bottom - ((interY / maxY) * (bottom - top)).toFloat()
            path.lineTo(px, py)
            
            canvas.drawPath(path, pathPaint)
            canvas.drawCircle(px, py, 13f, projectilePaint)
        } else {
            canvas.drawPath(path, pathPaint)
            trajectory.getOrNull(visibleCount - 1)?.let { point ->
                val px = left + ((point.x / maxX) * (right - left)).toFloat()
                val py = bottom - ((point.y / maxY) * (bottom - top)).toFloat()
                canvas.drawCircle(px, py, 13f, projectilePaint)
            }
        }
    }

    private fun drawAxes(canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float) {
        canvas.drawLine(left, top - 10f, left, bottom, axisPaint)
        canvas.drawLine(left, bottom, right + 10f, bottom, axisPaint)
    }

    private fun drawGrid(canvas: Canvas, left: Float, right: Float, top: Float, bottom: Float, maxX: Double, maxY: Double) {
        val columns = 5
        val rows = 5
        
        repeat(columns + 1) { index ->
            val x = left + ((right - left) * index / columns)
            canvas.drawLine(x, top, x, bottom, gridPaint)
            
            val labelValue = (maxX * index / columns)
            val label = String.format(Locale.US, "%.1f", labelValue)
            canvas.drawText(label, x, bottom + 35f, textPaint)
        }
        
        val yTextPaint = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }
        repeat(rows + 1) { index ->
            val y = bottom - ((bottom - top) * index / rows)
            canvas.drawLine(left, y, right, y, gridPaint)
            
            val labelValue = (maxY * index / rows)
            val label = String.format(Locale.US, "%.1f", labelValue)
            canvas.drawText(label, left - 15f, y + 8f, yTextPaint)
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
