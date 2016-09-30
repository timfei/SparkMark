package com.tim.annotation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by TimFei on 2016/9/27.
 */

public class AnnotatedView extends View {

    private Bitmap loadedBitmap;
    private Path path;
    private Paint paint;

    private int screenWidth;
    private int screenHeight;
    private int bitmapWidth;
    private int bitmapHeight;


    public AnnotatedView(Context context) {
        super(context);
    }

    public AnnotatedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    private void setup() {
        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (loadedBitmap != null) {
            canvas.drawBitmap(loadedBitmap, 0, (canvas.getHeight() - loadedBitmap.getHeight()) / 2, paint);
        }
        canvas.drawPath(path, paint);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(bitmapWidth, bitmapHeight);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                path.moveTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                path.lineTo(touchX, touchY);
                break;

            default:
                return false;
        }
        invalidate();
        return true;
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
