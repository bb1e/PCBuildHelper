package com.google.mediapipe.examples.objectdetection

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import kotlin.math.max

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: ObjectDetectorResult? = null
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var scaleFactor: Float = 1f
    private var bounds = Rect()
    private var outputWidth = 0
    private var outputHeight = 0
    private var outputRotate = 0
    private var textView: TextView? = null
    private val categoryNames = mutableListOf<String>()

    init {
        initPaints()
    }

    fun clear() {
        results = null
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.mp_primary)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.detections()?.map {
            val boxRect = RectF(
                it.boundingBox().left,
                it.boundingBox().top,
                it.boundingBox().right,
                it.boundingBox().bottom
            )
            val matrix = Matrix()
            matrix.postTranslate(-outputWidth / 2f, -outputHeight / 2f)

            matrix.postRotate(outputRotate.toFloat())

            if (outputRotate == 90 || outputRotate == 270) {
                matrix.postTranslate(outputHeight / 2f, outputWidth / 2f)
            } else {
                matrix.postTranslate(outputWidth / 2f, outputHeight / 2f)
            }
            matrix.mapRect(boxRect)
            boxRect
        }?.forEachIndexed { index, floats ->

            val top = floats.top * scaleFactor
            val bottom = floats.bottom * scaleFactor
            val left = floats.left * scaleFactor
            val right = floats.right * scaleFactor

            val drawableRect = RectF(left, top, right, bottom)
            canvas.drawRect(drawableRect, boxPaint)

            val category = results?.detections()!![index].categories()[0]
            val drawableText =
                category.categoryName() + " " + String.format(
                    "%.2f",
                    category.score()
                )

            textBackgroundPaint.getTextBounds(
                drawableText,
                0,
                drawableText.length,
                bounds
            )
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )

            canvas.drawText(
                drawableText,
                left,
                top + bounds.height(),
                textPaint
            )
        }
    }

    fun setResults(
        detectionResults: ObjectDetectorResult,
        textView: TextView,
        outputHeight: Int,
        outputWidth: Int,
        imageRotation: Int
    ) {
        results = detectionResults
        this.textView = textView
        this.outputWidth = outputWidth
        this.outputHeight = outputHeight
        this.outputRotate = imageRotation

        categoryNames.clear()

        results?.detections()?.forEach { detection ->
            detection.categories().forEach { category ->
                categoryNames.add(category.categoryName())
            }
        }

        if (categoryNames.isNotEmpty()) {
            if (!categoryNames.contains("cpu") && !categoryNames.contains("cpu_cooler")){
                textView.text = "No CPU detected! All computers need a brain!"
            } else if (!categoryNames.contains("cpu_cooler")){
                textView.text =  "No CPU Cooler detected, a cooler is required to keep your cpu from running hot! Make sure to apply thermal paste"
            } else if (!categoryNames.contains("disk_drive")){
                textView.text = "Don't forget to add storage!"
            } else if (!categoryNames.contains("gpu")){
                textView.text = "No dedicated GPU detected, make sure your cpu has integrated graphics!"
            } else if (!categoryNames.contains("motherboard")){
                textView.text = "No motherboard detected, a motherboard is the base of any computer"
            } else if (!categoryNames.contains("psu")){
                textView.text = "Make sure you have a PSU supplying energy to your computer"
            } else if (!categoryNames.contains("ram_stick")){
                textView.text = "Make sure you have at least a stick of ram"
            }
        } else {
            ""
        }
        /*textView.text = if (categoryNames.isNotEmpty()) {
            categoryNames.joinToString(", ")
        } else {
            ""
        }*/

        val rotatedWidthHeight = when (imageRotation) {
            0, 180 -> Pair(outputWidth, outputHeight)
            90, 270 -> Pair(outputHeight, outputWidth)
            else -> return
        }

        scaleFactor = max(
            width * 1f / rotatedWidthHeight.first,
            height * 1f / rotatedWidthHeight.second
        )
        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
