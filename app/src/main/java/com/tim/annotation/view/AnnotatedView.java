package com.tim.annotation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.tim.annotation.constants.Constant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by TimFei on 2016/9/27.
 */

public class AnnotatedView extends View {

    private Path mDrawPath;

    private Paint mDrawPaint, mCanvasPaint;

    private Canvas mDrawCanvas;

    private Bitmap mCanvasBitmap;

    private Bitmap mLoadBitmap;

    private static final int PRE_SIZE = 10;

    private int currentToolCode = 10;

    private float startX;

    private float startY;

    private static List<DrawPath> savePath;

    private static List<DrawPath> deletePath;

    private DrawPath dp;

    private int screenWidth;

    private int screenHeight;

    private float mX;
    private float mY;

    private class DrawPath {
        private Path path;
        private Paint paint;
    }

    public AnnotatedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        savePath = new ArrayList<>();
        deletePath = new ArrayList<>();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        initCanvas();
    }


    private void initCanvas() {
        mDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawPaint.setColor(Color.RED);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeWidth(PRE_SIZE);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
        mCanvasBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(mCanvasBitmap);
        mDrawCanvas.drawColor(Color.TRANSPARENT);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLoadBitmap != null) {
            int bitmapWidth = mLoadBitmap.getWidth();
            int bitmapHeight = mLoadBitmap.getHeight();
            int width = getWidth();
            int height = getHeight();
            canvas.drawBitmap(mLoadBitmap, (width - bitmapWidth) / 2, (height - bitmapHeight) / 2, null);
        }
        canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
        if (mDrawPath != null) {
            canvas.drawPath(mDrawPath, mDrawPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                mDrawPath = new Path();
                dp = new DrawPath();
                dp.path = mDrawPath;
                dp.paint = mDrawPaint;
                touch_start(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    private void touch_up() {
        mDrawPath.lineTo(mX, mY);
        mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
        savePath.add(dp);
        deletePath.clear();
        mDrawPath = null;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(mY - y);
        if (dx >= 4 || dy >= 4) {
            if (currentToolCode == Constant.CODE_TOOL_GESTURE) {
                mDrawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            } else if (currentToolCode == Constant.CODE_TOOL_ARROW) {
                mDrawPath.reset();
                drawArrow((int) startX, (int) startY, (int) x, (int) y);
            }
            mX = x;
            mY = y;
        }
    }

    private void touch_start(float x, float y) {
        mDrawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }


    public void unDo() {
        if (savePath != null && savePath.size() > 0) {
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);
            reDrawOnBitmap();
        }
    }


    public void recover() {
        if (deletePath.size() > 0) {
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            savePath.add(dp);
            mDrawCanvas.drawPath(dp.path, dp.paint);
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }


    public void reDo() {
        if (savePath != null && savePath.size() > 0) {
            savePath.clear();
            reDrawOnBitmap();
        }
    }


    private void reDrawOnBitmap() {
        initCanvas();
        Iterator<DrawPath> iterator = savePath.iterator();
        while (iterator.hasNext()) {
            DrawPath drawPath = iterator.next();
            mDrawCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();
    }

    public void drawArrow(int startX, int startY, int endX, int endY) {
        double lineLength = Math.sqrt(Math.pow(Math.abs(endX - startX), 2) + Math.pow(Math.abs(endY - startY), 2));
        double H = 0;
        double L = 0;
        if (lineLength < 320) {
            H = lineLength / 4;
            L = lineLength / 6;
        } else {
            H = 80;
            L = 50;
        }
        double arrowAngle = Math.atan(L / H);
        double arrowLen = Math.sqrt(L * L + H * H);
        double[] pointXY1 = rotateVec(endX - startX, endY - startY, arrowAngle, true, arrowLen);
        double[] pointXY2 = rotateVec(endX - startX, endY - startY, -arrowAngle, true, arrowLen);
        int x3 = (int) (endX - pointXY1[0]);
        int y3 = (int) (endY - pointXY1[1]);
        int x4 = (int) (endX - pointXY2[0]);
        int y4 = (int) (endY - pointXY2[1]);
        mDrawPath.moveTo(startX, startY);
        mDrawPath.lineTo(endX, endY);
        mDrawPath.moveTo(x3, y3);
        mDrawPath.lineTo(endX, endY);
        mDrawPath.lineTo(x4, y4);

    }

    public double[] rotateVec(int x, int y, double ang, boolean isChLen, double newLen) {
        double pointXY[] = new double[2];
        double vx = x * Math.cos(ang) - y * Math.sin(ang);
        double vy = x * Math.sin(ang) + y * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            pointXY[0] = vx / d * newLen;
            pointXY[1] = vy / d * newLen;
        }
        return pointXY;
    }


    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mLoadBitmap = bitmap;
            invalidate();
        }
    }

    public int getSavePathCount() {
        if (savePath != null) {
            return savePath.size();
        }
        return 0;
    }

    public void setTool(int toolCode) {
        this.currentToolCode = toolCode;
    }


}
