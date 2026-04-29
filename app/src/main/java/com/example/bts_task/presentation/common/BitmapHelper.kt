package com.example.bts_task.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

object BitmapHelper {

    fun fromVector(context: Context, @DrawableRes resId: Int, sizeDp: Float = 32f): Bitmap {
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            sizeDp,
            context.resources.displayMetrics
        ).toInt().coerceAtLeast(8)
        val drawable = ContextCompat.getDrawable(context, resId)!!
        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, px, px)
        drawable.draw(canvas)
        return bitmap
    }

    fun dot(context: Context, colorArgb: Int, sizeDp: Float = 18f): Bitmap {
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            sizeDp,
            context.resources.displayMetrics
        ).toInt().coerceAtLeast(8)

        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val r = px / 2f

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
        canvas.drawCircle(r, r, r, borderPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorArgb }
        canvas.drawCircle(r, r, r * 0.78f, fillPaint)

        return bitmap
    }
}
