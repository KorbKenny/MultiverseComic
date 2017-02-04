package com.korbkenny.multiversecomic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Scott on 2/4/17.
 */

public class MultiverseView extends SurfaceView implements SurfaceHolder.Callback{

    private final static int NONE = 0;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private Paint mPaint;
    private Canvas mCanvas;
    public HashMap<GlobalPageObject, Bitmap> mPageBitmaps;
    public List<GlobalPageObject> mPages;


    public MultiverseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        new PageManager();
        mPaint = new Paint();
        beginDrawing();
        drawImages();
        finishDrawing();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    private void beginDrawing(){
        mCanvas = getHolder().lockCanvas();
        mCanvas.drawColor(Color.WHITE);
        mCanvas.save();
    }

    private void finishDrawing(){
        getHolder().unlockCanvasAndPost(mCanvas);
    }

    private void drawImages(){
        for (int i=0; i<mPageBitmaps.size(); i++){
            Bitmap bitmap = mPageBitmaps.get(mPages.get(i));
            if (bitmap != null) {
                if (i == 0) {
                    mCanvas.drawBitmap(bitmap, 0, i * 100, mPaint);
                } else {
                    int top = bitmap.getHeight() * i;
                    mCanvas.drawBitmap(bitmap, 0, top, mPaint);
                }
            }
        }
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            beginDrawing();

            mCanvas.scale(mScaleFactor, mScaleFactor);

            drawImages();

            finishDrawing();

            return true;
        }
    }



    private class PageManager {

        private FirebaseDatabase db;

        public PageManager() {
            mPageBitmaps = new HashMap<>();
            mPages = new ArrayList<>();
            fetchPagesSetUp();
        }

        private void fetchPagesSetUp(){
            new AsyncTask<Void, Void, Void>(){

                @Override
                protected Void doInBackground(Void... voids) {
                    db = FirebaseDatabase.getInstance();
                    DatabaseReference dbRef =
                            db.getReference(Constants.GLOBAL).child(Constants.FIRST_PAGE_ID);
                    fetchPages(dbRef);
                    return null;
                }
            }.execute();
        }

        private void fetchPages(final DatabaseReference dbRef){
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null){
                        final GlobalPageObject page = dataSnapshot.getValue(GlobalPageObject.class);

                        if (page != null) {

                            mPages.add(page);

                            Picasso.with(getContext()).load(page.getImage()).placeholder(R.drawable.drawplaceholder).into(new com.squareup.picasso.Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    mPageBitmaps.put(page, bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                    Log.d(TAG, "onBitmapFailed: FAILED");
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {}
                            });

                            if (!page.getNextRight().equals(Constants.DB_NULL)) {
                                Log.d(TAG, "onDataChange: "+page.getNextRight());
                                DatabaseReference rightDbRef =
                                        db.getReference(Constants.GLOBAL).child(page.getNextRight());
                                fetchPages(rightDbRef);
                            }
                            if (!page.getNextLeft().equals(Constants.DB_NULL)) {
                                Log.d(TAG, "onDataChange: "+page.getNextLeft());
                                DatabaseReference leftDbRef =
                                        db.getReference(Constants.GLOBAL).child(page.getNextLeft());
                                fetchPages(leftDbRef);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }

    }
}
