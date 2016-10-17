package com.tim.annotation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tim.annotation.constants.Constant;

/**
 * Created by TimFei on 2016/9/27.
 */

public class AnnotatedView extends View {

    private Bitmap loadedBitmap;
    private Path mPath;
    private Paint mPaint;

    private int screenWidth;
    private int screenHeight;
    private int bitmapWidth;
    private int bitmapHeight;

    private int toolCode;
    private DrawPath dp;
    private float mX;
    private float mY;
    private static final float TOUCH_TOLERANCE = 4;
    private float startX;
    private float startY;

    private class DrawPath {
        public Path path;
        public Paint paint;
    }

    public AnnotatedView(Context context) {
        super(context);
    }

    public AnnotatedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.STROKE);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (loadedBitmap != null) {
            canvas.drawBitmap(loadedBitmap, 0, (canvas.getHeight() - loadedBitmap.getHeight()) / 2, mPaint);
        }
        canvas.drawPath(mPath, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                mPath = new Path();
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;

                touch_start(touchX, touchY);

//                mPath.moveTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                touch_move(touchX,touchY);
//                mPath.lineTo(touchX, touchY);
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setTool(int toolCode) {
        this.toolCode = toolCode;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(mY - y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            if (toolCode == Constant.CODE_TOOL_RECT) {
                mPath.reset();
                RectF rectF = new RectF(startX, startX, x, y);
                mPath.addRect(rectF, Path.Direction.CCW);
            }
        }

    }

    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_up(){
        
    }

    public void setBitmap(Bitmap bitmap, int x, int y) {
        this.loadedBitmap = bitmap;
        this.bitmapWidth = loadedBitmap.getWidth();
        this.bitmapHeight = loadedBitmap.getHeight();
        this.screenWidth = x;
        this.screenHeight = y;
        invalidate();
    }
}
