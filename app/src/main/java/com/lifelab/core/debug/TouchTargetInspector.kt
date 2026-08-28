package com.lifelab.core.debug

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import kotlin.math.roundToInt

/**
 * Debug-only runtime inspector for Android's recommended 48dp touch target.
 *
 * It reports undersized interactive views visually. It never changes a view's
 * size or consumes its touch events.
 */
class TouchTargetInspector(activity: Activity) {

    private val contentRoot = activity.findViewById<FrameLayout>(
        android.R.id.content,
    )
    private val density = activity.resources.displayMetrics.density
    private val minimumTouchTargetPx = MINIMUM_TOUCH_TARGET_DP * density
    private val overlay = TouchTargetOverlay(activity, density).apply {
        visibility = View.GONE
        isClickable = false
        isFocusable = false
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private var enabled = false
    private val scanRunnable = Runnable(::scan)
    private val globalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener(::scheduleScan)
    private val scrollChangedListener =
        ViewTreeObserver.OnScrollChangedListener(::scheduleScan)

    init {
        contentRoot.addView(
            overlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
    }

    fun isEnabled(): Boolean = enabled

    fun setEnabled(value: Boolean) {
        if (enabled == value) {
            if (value) scheduleScan()
            return
        }

        enabled = value

        if (value) {
            overlay.visibility = View.VISIBLE
            contentRoot.viewTreeObserver.addOnGlobalLayoutListener(
                globalLayoutListener,
            )
            contentRoot.viewTreeObserver.addOnScrollChangedListener(
                scrollChangedListener,
            )
            scheduleScan()
        } else {
            removeListeners()
            contentRoot.removeCallbacks(scanRunnable)
            overlay.show(emptyList())
            overlay.visibility = View.GONE
        }
    }

    fun dispose() {
        setEnabled(false)
        contentRoot.removeView(overlay)
    }

    private fun scheduleScan() {
        if (!enabled) return

        contentRoot.removeCallbacks(scanRunnable)
        contentRoot.postDelayed(
            scanRunnable,
            SCAN_DELAY_MILLIS,
        )
    }

    private fun scan() {
        if (!enabled || overlay.width == 0 || overlay.height == 0) return

        val overlayLocation = IntArray(2)
        overlay.getLocationOnScreen(overlayLocation)

        val violations = mutableListOf<TouchTargetViolation>()
        collectViolations(
            view = contentRoot,
            overlayLocation = overlayLocation,
            result = violations,
        )
        overlay.show(violations)
    }

    private fun collectViolations(
        view: View,
        overlayLocation: IntArray,
        result: MutableList<TouchTargetViolation>,
    ) {
        if (
            view === overlay ||
            !view.isShown ||
            !view.isEnabled ||
            view.alpha <= 0f
        ) {
            return
        }

        val isInteractive =
            view.isClickable ||
                view.isLongClickable ||
                view.hasOnClickListeners()
        val renderedWidth = view.width.toFloat()
        val renderedHeight = view.height.toFloat()

        if (
            isInteractive &&
            renderedWidth > 0f &&
            renderedHeight > 0f &&
            (renderedWidth < minimumTouchTargetPx &&
                renderedHeight < minimumTouchTargetPx)
        ) {
            val visibleBounds = Rect()

            if (view.getGlobalVisibleRect(visibleBounds)) {
                visibleBounds.offset(
                    -overlayLocation[0],
                    -overlayLocation[1],
                )

                result += TouchTargetViolation(
                    bounds = RectF(visibleBounds),
                    widthDp = (renderedWidth / density).roundToInt(),
                    heightDp = (renderedHeight / density).roundToInt(),
                )
            }
        }

        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                collectViolations(
                    view = view.getChildAt(index),
                    overlayLocation = overlayLocation,
                    result = result,
                )
            }
        }
    }

    private fun removeListeners() {
        val observer = contentRoot.viewTreeObserver
        if (!observer.isAlive) return

        observer.removeOnGlobalLayoutListener(globalLayoutListener)
        observer.removeOnScrollChangedListener(scrollChangedListener)
    }

    private companion object {
        const val MINIMUM_TOUCH_TARGET_DP = 48f
        const val SCAN_DELAY_MILLIS = 80L
    }
}

private data class TouchTargetViolation(
    val bounds: RectF,
    val widthDp: Int,
    val heightDp: Int,
)

private class TouchTargetOverlay(
    activity: Activity,
    private val density: Float,
) : View(activity) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(42, 244, 67, 54)
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(211, 47, 47)
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val labelBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(211, 47, 47)
        style = Paint.Style.FILL
    }
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 11f * density
        isFakeBoldText = true
    }
    private var violations: List<TouchTargetViolation> = emptyList()

    fun show(value: List<TouchTargetViolation>) {
        violations = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val horizontalPadding = 4f * density
        val labelHeight = 18f * density

        violations.forEach { violation ->
            val bounds = violation.bounds
            canvas.drawRect(bounds, fillPaint)
            canvas.drawRect(bounds, borderPaint)

            val label = "${violation.widthDp} × ${violation.heightDp}dp"
            val labelWidth = labelTextPaint.measureText(label) +
                horizontalPadding * 2
            val labelLeft = bounds.left.coerceIn(
                0f,
                (width - labelWidth).coerceAtLeast(0f),
            )
            val labelTop = bounds.top.coerceIn(
                0f,
                (height - labelHeight).coerceAtLeast(0f),
            )

            canvas.drawRect(
                labelLeft,
                labelTop,
                labelLeft + labelWidth,
                labelTop + labelHeight,
                labelBackgroundPaint,
            )
            canvas.drawText(
                label,
                labelLeft + horizontalPadding,
                labelTop + 13f * density,
                labelTextPaint,
            )
        }
    }
}
