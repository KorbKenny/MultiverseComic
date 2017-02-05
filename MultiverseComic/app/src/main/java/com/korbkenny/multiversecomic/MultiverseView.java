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

import static android.R.attr.bitmap;
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
    public HashMap<MultiversePage, Bitmap> mPageBitmaps;
    public List<MultiversePage> mPages;


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

    private void drawImages() {
        Log.d(TAG, "drawImages: "+mPageBitmaps.size());
        if (!mPageBitmaps.isEmpty()) {
            Bitmap bitmap = mPageBitmaps.get(mPages.get(0));
            Bitmap bitmapRow2Right = mPageBitmaps.get(mPages.get(1));
            Bitmap bitmapRow2Left = mPageBitmaps.get(mPages.get(2));
            Log.d(TAG, "PAGE RIGHT "+mPages.get(0).getPageRight());
            Log.d(TAG, "RIGHT BITMAP"+bitmapRow2Right);
            Log.d(TAG, "PAGE LEFT "+mPages.get(0).getPageLeft());
            Log.d(TAG, "LEFT BITMAP"+bitmapRow2Left);
            if (bitmap != null) {
                mCanvas.drawBitmap(bitmap, (getWidth() / 2) - (bitmap.getWidth() / 2), 0, mPaint);
            }

            if (bitmapRow2Right != null) {
                Log.d(TAG, "drawImages: RIGHT");
                //draw second row right
                int width = bitmapRow2Right.getWidth()/2;
                int height = bitmapRow2Right.getHeight()/2;
                bitmapRow2Right = Bitmap.createScaledBitmap(bitmapRow2Right, width, height, true);
                mCanvas.drawBitmap(bitmapRow2Right, (getWidth() / 2), bitmap.getHeight(), mPaint);
            }

            if (bitmapRow2Left != null) {
                Log.d(TAG, "drawImages: LEFT");
                //draw second row left
                int width = bitmapRow2Left.getWidth()/2;
                int height = bitmapRow2Left.getHeight()/2;
                bitmapRow2Left = Bitmap.createScaledBitmap(bitmapRow2Left, width, height, true);
                mCanvas.drawBitmap(bitmapRow2Left, (getWidth() / 2) - (bitmapRow2Left.getWidth()),
                        bitmap.getHeight(), mPaint);
            }


            //recurse to right and left until page == null
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

                @Override
                protected void onPostExecute(Void aVoid) {

                }
            }.execute();
        }

        private void fetchPages(final DatabaseReference dbRef){
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null){
                        final MultiversePage page = dataSnapshot.getValue(MultiversePage.class);

                        if (page != null) {

                            assignPageRightAndLeft(page);

                            mPages.add(page);

                            Picasso.with(getContext()).load(page.getImage())
                                    .into(new com.squareup.picasso.Target() {
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

        private void assignPageRightAndLeft(final MultiversePage page){
            if (!page.getNextRight().equals(Constants.DB_NULL)) {
                db.getReference(Constants.GLOBAL).child(page.getNextRight()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        MultiversePage pageRight = dataSnapshot.getValue(MultiversePage.class);
                        page.setPageRight(pageRight);
                        Log.d(TAG, "onDataChange: RIGHT ASSIGNED");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }

            if (!page.getNextLeft().equals(Constants.DB_NULL)) {
                db.getReference(Constants.GLOBAL).child(page.getNextLeft()).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        MultiversePage pageLeft = dataSnapshot.getValue(MultiversePage.class);
                        page.setPageLeft(pageLeft);
                        Log.d(TAG, "onDataChange: LEFT ASSIGNED");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
        }
    }
}
