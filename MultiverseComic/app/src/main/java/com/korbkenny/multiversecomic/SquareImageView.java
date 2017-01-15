package com.korbkenny.multiversecomic;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by KorbBookProReturns on 1/15/17.
 */

public class SquareImageView extends ImageView {

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width,width);
    }
}
