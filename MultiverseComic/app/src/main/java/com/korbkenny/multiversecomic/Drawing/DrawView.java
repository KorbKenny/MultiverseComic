package com.korbkenny.multiversecomic.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

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
        mBrushSize = 8;
        mPaintAlpha = 255;
        mLastBrushSize = mBrushSize;

        mDrawPath = new Path();
        mDrawPaint = new Paint();
        mMoveList = new ArrayList<>();
        mCurrentMoveList = new ArrayList<>();

        setBrushSize(8);

        //  Will use this for redo function
        mUndoList = new ArrayList<>();

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
        //  It actually redraws every path in mMoveList
        //  every time invalidate() is called.
        for(PathPaint pp:mMoveList){
            mDrawPaint.setStrokeWidth(pp.getBrushSize());
            mDrawPaint.setColor(pp.getPaintColor());
            mDrawPaint.setAlpha(pp.getPaintAlpha());
            canvas.drawPath(pp.getPath(),pp.getPaint());
        }

        //  And then it draws the current line. I do this
        //  because opacity wasn't working otherwise.
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
                //  When you touch the screen, the path will now start
                //  at the xy that your finger touched.
                mDrawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                //  When you move your finger, it draws a path with the current brush stuff,
                //  and adds it to the current move list to be redrawn after.
                mDrawPath.lineTo(touchX, touchY);
                mCurrentMoveList.add(new PathPaint(mDrawPath,mDrawPaint,mBrushSize,mPaintColor,mPaintAlpha));
                break;
            case MotionEvent.ACTION_UP:
                //  Add the path you just drew to the move list so you can undo it later.
                //  Reset everything for the next path.
                mMoveList.add(new PathPaint(mDrawPath,mDrawPaint,mBrushSize,mPaintColor,mPaintAlpha));
                mDrawPath = new Path();
                mDrawPath.reset();
                mCurrentMoveList.clear();
                break;
            default:
                return false;
        }
        //  invalidate() calls the onDraw callback.
        invalidate();
        return true;
    }

    public void undo(){
        if (mMoveList.size() > 0){
            mMoveList.remove(mMoveList.size()-(1));
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
}
