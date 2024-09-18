package com.valdesius.noteapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
    private static final String TAG = "CanvasView";
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xFF660000;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            Log.d(TAG, "onSizeChanged: canvasBitmap created with width=" + w + " and height=" + h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
            canvas.drawPath(drawPath, drawPaint);
            Log.d(TAG, "onDraw: drawing on canvas");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                Log.d(TAG, "onTouchEvent: ACTION_DOWN at x=" + touchX + ", y=" + touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                Log.d(TAG, "onTouchEvent: ACTION_MOVE at x=" + touchX + ", y=" + touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                Log.d(TAG, "onTouchEvent: ACTION_UP");
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public Bitmap getBitmap() {
        return canvasBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }
}
