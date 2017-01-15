package com.korbkenny.multiversecomic.Drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by KorbBookProReturns on 1/11/17.
 */

public class DrawView extends View{

    private Path mDrawPath;
    private Paint mDrawPaint, mCanvasPaint;
    private int mPaintColor = 0xFF000000;
    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;
    private List<PathPaint> mMoveList, mUndoList, mCurrentMoveList;
    private float mBrushSize, mLastBrushSize;
    private int mUndoCounter = 0;
    private int  mPaintAlpha;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width,width);
    }

    private void setupDrawing(){
        mBrushSize = 15;
        mPaintAlpha = 255;
        mLastBrushSize = mBrushSize;

        mDrawPath = new Path();
        mDrawPaint = new Paint();
        mMoveList = new ArrayList<>();
        mUndoList = new ArrayList<>();
        mCurrentMoveList = new ArrayList<>();

        mDrawPaint.setStrokeWidth(mBrushSize);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setAlpha(mPaintAlpha);

        mCanvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(mCanvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawBitmap(mCanvasBitmap,0,0,mCanvasPaint);
//        canvas.drawPath(mDrawPath,mDrawPaint);
        for(PathPaint pp:mMoveList){
            mDrawPaint.setStrokeWidth(pp.getBrushSize());
            mDrawPaint.setColor(pp.getPaintColor());
            mDrawPaint.setAlpha(pp.getPaintAlpha());
            canvas.drawPath(pp.getPath(),pp.getPaint());
        }
        for(PathPaint pp:mCurrentMoveList){
            mDrawPaint.setStrokeWidth(pp.getBrushSize());
            mDrawPaint.setColor(pp.getPaintColor());
            mDrawPaint.setAlpha(pp.getPaintAlpha());
            canvas.drawPath(pp.getPath(),pp.getPaint());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                mDrawPath.lineTo(touchX, touchY);
                mCurrentMoveList.add(new PathPaint(mDrawPath,mDrawPaint,mBrushSize,mPaintColor,mPaintAlpha));
                break;
            case MotionEvent.ACTION_UP:
                mUndoCounter = 0;
//                mDrawCanvas.drawPath(mDrawPath, mDrawPaint);
                mMoveList.add(new PathPaint(mDrawPath,mDrawPaint,mBrushSize,mPaintColor,mPaintAlpha));
                mDrawPath = new Path();
                mDrawPath.reset();
                mCurrentMoveList.clear();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void undo(){
        if (mMoveList.size() > 0){
            mUndoCounter++;
            mMoveList.remove(mMoveList.size()-(1));
            invalidate();
        }
    }

    public void clearEverything(){
        if (mMoveList.size() > 0){
            mUndoCounter = 0;
            mMoveList.clear();
            invalidate();
        }
    }

    public void setColor(String newColor){
        mPaintColor = Color.parseColor(newColor);
        mDrawPaint.setColor(mPaintColor);
    }

    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        mBrushSize = pixelAmount;
        mDrawPaint.setStrokeWidth(mBrushSize);
    }

    public void setOpacity(int newAlpha){
        mPaintAlpha = Math.round((float)newAlpha/100*255);
        mDrawPaint.setAlpha(mPaintAlpha);
    }

    public void setLastBrushSize(float lastSize){
        mLastBrushSize=lastSize;
    }

    public float getLastBrushSize(){
        return mLastBrushSize;
    }
}
