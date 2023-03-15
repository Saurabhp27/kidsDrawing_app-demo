package com.example.kidsdrawingapp

import android.content.Context
import android.content.pm.LauncherApps.PinItemRequest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context : Context, attrs : AttributeSet ): View(context, attrs) {

    private var mDrawPath : CustomPath? = null
    private var mCanvasBitmap : Bitmap? = null
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize : Float = 0.toFloat()
    private var Canvas : Canvas? = null
    private var color = Color.BLACK
    private val mpath = ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

    private fun setupDrawing (){
        mDrawPath = CustomPath(color, mBrushSize)

        mDrawPaint = Paint()
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND

        mCanvasPaint = Paint(Paint.DITHER_FLAG)
//        mBrushSize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w , h , Bitmap.Config.ARGB_8888)
        Canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        for (paths in mpath){
            mDrawPaint!!.strokeWidth = paths.brushThickness
            mDrawPaint!!.color = paths.color
            canvas?.drawPath(paths, mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty()){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath?.color = color
                mDrawPath?.brushThickness = mBrushSize

                mDrawPath?.reset() // Clear any lines and curves from the path, making it empty.
                touchX?.let {
                    touchY?.let { it1 ->
                        mDrawPath?.moveTo(
                            it,
                            it1
                        )
                    }
                } // Set the beginning of the next contour to the point (x,y).
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath?.lineTo(
                            touchX,
                            touchY
                        )
                    }
                } // Add a line from the last point to the specified point (x,y).
            }

            MotionEvent.ACTION_UP -> {
                mpath.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }

        invalidate()
        return true

    }


fun setsizeofbrush (newsize : Float){
    mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newsize, resources.displayMetrics)
    mDrawPaint!!.strokeWidth = mBrushSize
}


    internal inner class CustomPath (var color : Int, var brushThickness : Float) : Path() {

    }

    }
