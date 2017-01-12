package com.korbkenny.multiversecomic.Drawing;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by KorbBookProReturns on 1/11/17.
 */

public class PathPaint {
    private Path mPath;
    private Paint mPaint;
    private float mBrushSize;

    public PathPaint(Path path, Paint paint, float brushSize) {
        mPath = path;
        mPaint = paint;
        mBrushSize = brushSize;
    }

    public Path getPath() {
        return mPath;
    }

    public void setPath(Path path) {
        mPath = path;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public float getBrushSize() {
        return mBrushSize;
    }

    public void setBrushSize(float brushSize) {
        mBrushSize = brushSize;
    }
}

